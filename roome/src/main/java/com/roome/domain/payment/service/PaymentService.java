package com.roome.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.repository.PaymentLogRepository;
import com.roome.domain.payment.repository.PaymentRepository;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentLogRepository paymentLogRepository;
  private final UserRepository userRepository;
  private final PointRepository pointRepository;
  private final TossPaymentClient tossPaymentClient;
  private final PointService pointService;


  // 사용자가 포인트 결제를 요청하면 DB에 저장해 줌
  // 결제 진행 중인 상태
  @Transactional
  public PaymentResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    Payment payment = Payment.builder()
        .user(user)
        .orderId(requestDto.getOrderId())
        .amount(requestDto.getAmount())
        .purchasedPoints(requestDto.getPurchasedPoints())
        .status(PaymentStatus.PENDING)
        .paymentKey(null) // 결제 성공 후 업데이트 예정
        .build();

    paymentRepository.save(payment);
    log.info("결제 요청 저장 완료: orderId={}, amount={}, userId={}",
        payment.getOrderId(), payment.getAmount(), user.getId());

    return PaymentResponseDto.builder()
        .orderId(payment.getOrderId())
        .amount(payment.getAmount())
        .purchasedPoints(payment.getPurchasedPoints())
        .status(payment.getStatus())
        .build();
  }


  // 결제 성공 후, 토스 API의 응답을 검증하고 포인트 지급
  @Transactional
  public PaymentResponseDto verifyPayment(Long userId, PaymentVerifyDto verifyDto) {
    log.info("결제 검증 요청: paymentKey={}, orderId={}, amount={}",
            verifyDto.getPaymentKey(), verifyDto.getOrderId(), verifyDto.getAmount());

    Payment payment = paymentRepository.findByOrderId(verifyDto.getOrderId())
        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    log.info("DB 조회 결과: orderId={}, amount={}, paymentKey={}",
            payment.getOrderId(), payment.getAmount(), payment.getPaymentKey());

    // 결제 금액 검증
    if (payment.getAmount() != verifyDto.getAmount()) {
      throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }


    //토스에 결제 승인 요청
    try {
      ResponseEntity<String> response = tossPaymentClient.requestConfirm(verifyDto);

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error("❌ 결제 승인 실패 - Status={}, Response={}", response.getStatusCode(), response.getBody());
        throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
      }
      log.info("✅ 결제 승인 성공 - paymentKey={}", verifyDto.getPaymentKey());
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
    }

    /*
    // 토스 API에서 결제 상태 확인
    log.info("토스 결제 검증 요청: paymentKey={}, orderId={}, amount={}",
            verifyDto.getPaymentKey(), verifyDto.getOrderId(), verifyDto.getAmount());

    boolean isVerified = tossPaymentClient.verifyPayment(
            verifyDto.getPaymentKey(), verifyDto.getOrderId(), verifyDto.getAmount()
    );
    if (!isVerified) {
      log.error("토스 결제 검증 실패: orderId={}, paymentKey={}", verifyDto.getOrderId(),
          verifyDto.getPaymentKey());
      throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
    }
    */

    // 결제 상태 업데이트
    payment.updateStatus(PaymentStatus.SUCCESS);
    payment.updatePaymentKey(verifyDto.getPaymentKey());
    paymentRepository.save(payment);

    // 사용자 포인트 지급
    PointReason pointReason = getPointReasonForAmount(payment.getPurchasedPoints());
    pointService.earnPoints(payment.getUser(), pointReason);

    // 결제 내역 로그 저장
    savePaymentLog(payment, verifyDto.getPaymentKey());

    log.info("결제 성공 및 포인트 지급 완료: orderId={}, userId={}, pointsAdded={}",
        verifyDto.getOrderId(), userId, payment.getPurchasedPoints());

    return PaymentResponseDto.builder()
        .orderId(payment.getOrderId())
        .paymentKey(verifyDto.getPaymentKey())
        .amount(payment.getAmount())
        .purchasedPoints(payment.getPurchasedPoints())
        .status(payment.getStatus())
        .build();
  }

  // 결제 실패 처리
  @Transactional
  public void failPayment(String orderId) {
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    payment.updateStatus(PaymentStatus.FAILED);
    paymentRepository.save(payment);

    log.warn("결제 실패: orderId={}", orderId);
  }

  // 결제 취소 (환불)
  @Transactional
  public PaymentResponseDto cancelPayment(Long userId, String orderId, String cancelReason,
      Integer cancelAmount) {
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    if (!payment.getUser().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
    }

    // 결제 상태가 SUCCESS가 아닐 경우, 취소 불가능
    if (!payment.getStatus().equals(PaymentStatus.SUCCESS)) {
      throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
    }

    // Toss API에 결제 취소 요청
    boolean isCanceled = tossPaymentClient.cancelPayment(payment.getPaymentKey(), cancelReason,
        cancelAmount);
    if (!isCanceled) {
      throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    // 결제 상태 업데이트
    payment.updateStatus(PaymentStatus.CANCELED);
    paymentRepository.save(payment);

    // 사용자 포인트 차감
    int refundAmount = (cancelAmount != null) ? cancelAmount : payment.getPurchasedPoints();
    pointService.usePoints(payment.getUser(), getRefundReasonForAmount(refundAmount));

    log.info("결제 취소 완료: orderId={}, userId={}, refundAmount={}", orderId, userId, refundAmount);

    return PaymentResponseDto.builder()
        .orderId(payment.getOrderId())
        .paymentKey(payment.getPaymentKey())
        .amount(payment.getAmount())
        .purchasedPoints(payment.getPurchasedPoints())
        .status(PaymentStatus.CANCELED)
        .build();
  }

  private void savePaymentLog(Payment payment, String paymentKey) {
    PaymentLog paymentLog = PaymentLog.builder()
            .user(payment.getUser())
            .amount(payment.getAmount())
            .earnedPoints(payment.getPurchasedPoints())
            .paymentKey(paymentKey)
            .build();
    paymentLogRepository.save(paymentLog);
  }

  private PointReason getPointReasonForAmount(int purchasedPoints) {
    return switch (purchasedPoints) {
      case 100 -> PointReason.POINT_PURCHASE_100;
      case 550 -> PointReason.POINT_PURCHASE_550;
      case 1200 -> PointReason.POINT_PURCHASE_1200;
      case 4000 -> PointReason.POINT_PURCHASE_4000;
      default -> throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
    };
  }

  private PointReason getRefundReasonForAmount(int refundPoints) {
    return switch (refundPoints) {
      case 100 -> PointReason.POINT_REFUND_100;
      case 550 -> PointReason.POINT_REFUND_550;
      case 1200 -> PointReason.POINT_REFUND_1200;
      case 4000 -> PointReason.POINT_REFUND_4000;
      default -> throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
    };
  }
}
