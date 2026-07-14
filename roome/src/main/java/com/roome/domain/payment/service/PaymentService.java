package com.roome.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.payment.dto.PaymentLogResponseDto;
import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.entity.PointProduct;
import com.roome.domain.payment.repository.PaymentLogRepository;
import com.roome.domain.payment.repository.PaymentRepository;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
  private final PointHistoryRepository pointHistoryRepository;

  // 사용자가 포인트 결제를 요청하면 DB에 저장해 줌
  // 결제 진행 중인 상태
  @Transactional
  public PaymentResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 금액이 판매 중인 상품 가격인지 검증하고, 지급 포인트는 클라이언트 값이 아닌 카탈로그에서 파생한다.
    PointProduct product = PointProduct.findByPrice(requestDto.getAmount())
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT));

    if (product.getPoints() != requestDto.getPurchasedPoints()) {
      log.warn("포인트 수량 위변조 의심: userId={}, orderId={}, 요청 포인트={}, 카탈로그 포인트={}",
          userId, requestDto.getOrderId(), requestDto.getPurchasedPoints(), product.getPoints());
    }

    Payment payment = Payment.builder()
        .user(user)
        .orderId(requestDto.getOrderId())
        .amount(product.getPrice())
        .purchasedPoints(product.getPoints())
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
    log.info("✅ Step 1: 결제 검증 시작");
    Payment payment = paymentRepository.findByOrderId(verifyDto.getOrderId())
            .orElseThrow(() -> {
              log.error("❌ Step 2: 결제 정보 없음 - orderId={}", verifyDto.getOrderId());
              return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
            });

    log.info("✅ Step 3: DB 조회 완료 - orderId={}, amount={}, paymentKey={}",
            payment.getOrderId(), payment.getAmount(), payment.getPaymentKey());

    if (payment.getAmount() != verifyDto.getAmount()) {
      log.error("❌ Step 4: 결제 금액 불일치 - 요청 금액={}, 저장된 금액={}",
              verifyDto.getAmount(), payment.getAmount());
      throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }

    log.info("✅ Step 5: Toss 결제 승인 요청 시작");
    try {
      ResponseEntity<String> response = tossPaymentClient.requestConfirm(verifyDto);
      log.info("✅ Step 6: Toss API 응답 수신 - Status={}, Body={}",
              response.getStatusCode(), response.getBody());

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error("❌ Step 7: 결제 승인 실패 - Status={}, Response={}",
                response.getStatusCode(), response.getBody());
        throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
      }

      JsonNode jsonResponse = new ObjectMapper().readTree(response.getBody());
      String paymentStatus = jsonResponse.get("status").asText();
      log.info("✅ Step 8: Toss 응답 상태 확인 - paymentKey={}, status={}", verifyDto.getPaymentKey(), paymentStatus);

      if (!"DONE".equals(paymentStatus)) {
        log.error("❌ Step 9: 결제 상태 검증 실패 - orderId={}, paymentKey={}, status={}",
                verifyDto.getOrderId(), verifyDto.getPaymentKey(), paymentStatus);

        throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
      }
      log.info("✅ Step 10: 결제 승인 성공 및 상태 확인 완료");
    } catch (Exception e) {
      log.error("❌ Step 11: 결제 승인 중 예외 발생: {}", e.getMessage());
      throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
    }

    log.info("✅ Step 12: 결제 상태 업데이트 및 포인트 지급 시작");

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


    // 결제 상태 업데이트
    payment.updateStatus(PaymentStatus.SUCCESS);
    payment.updatePaymentKey(verifyDto.getPaymentKey());
    paymentRepository.save(payment);

    // 사용자 포인트 지급
    // Toss가 승인한 결제 금액(payment.amount)을 기준으로 카탈로그에서 지급 사유를 파생
    PointProduct product = PointProduct.findByPrice(payment.getAmount())
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT));
    pointService.earnPoints(payment.getUser(), product.getEarnReason());

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
  public PaymentResponseDto cancelPayment(Long userId, String paymentKey, String cancelReason,
      Integer cancelAmount) {
    Payment payment = paymentRepository.findByPaymentKey(paymentKey)
        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    if (!payment.getUser().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
    }

    List<PointReason> purchaseReasons = PointProduct.purchaseReasons();
    PageRequest pageRequest = PageRequest.of(0, 1); // 최신 1개만 조회

    List<PointHistory> latestPurchases = pointHistoryRepository.findLatestPurchase(userId, purchaseReasons, pageRequest);

    Optional<PointHistory> latestPurchase = latestPurchases.isEmpty() ? Optional.empty() : Optional.of(latestPurchases.get(0));

    if (latestPurchase.isEmpty()) {
      throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
    }

    PointHistory lastPurchase = latestPurchase.get();

    // 환불 가능 기간 체크
    if (lastPurchase.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
      throw new BusinessException(ErrorCode.PAYMENT_REFUND_PERIOD_EXCEEDED);
    }

    // 포인트 사용 여부 체크
    boolean hasUsedPoints = pointHistoryRepository.hasUsedPointsAfterLastPurchase(userId);
    if (hasUsedPoints) {
      throw new BusinessException(ErrorCode.PAYMENT_ALREADY_USED);
    }

    // 결제 상태가 SUCCESS가 아닐 경우, 취소 불가능
    if (!payment.getStatus().equals(PaymentStatus.SUCCESS)) {
      throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
    }

    // 환불 금액이 판매 중인 상품 가격 단위인지 검증하고, 차감 포인트를 카탈로그에서 파생한다.
    if (cancelAmount == null) {
      throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
    }
    PointProduct refundProduct = PointProduct.findByPrice(cancelAmount)
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT));
    int refundPoints = refundProduct.getPoints();

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
    pointService.usePoints(payment.getUser(), refundProduct.getRefundReason());

    saveRefundLog(payment, cancelAmount, paymentKey);

    log.info("결제 취소 완료: paymentKey={}, userId={}, refundPoints={}, refundAmount={}",
            paymentKey, userId, refundPoints, cancelAmount);

    return PaymentResponseDto.builder()
        .orderId(payment.getOrderId())
        .paymentKey(payment.getPaymentKey())
        .amount(payment.getAmount())
        .purchasedPoints(payment.getPurchasedPoints())
        .status(PaymentStatus.CANCELED)
        .build();
  }

  @Transactional(readOnly = true)
  public List<PaymentLogResponseDto> getPaymentHistory(Long userId, int page, int size) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<PaymentLog> paymentLogs = paymentLogRepository.findByUser(user, pageRequest);

    return paymentLogs.stream()
            .map(log -> {
              // 환불 내역이면 CANCELED, 기존 결제면 SUCCESS 유지
              PaymentStatus status = log.isRefund() ? PaymentStatus.CANCELED : PaymentStatus.SUCCESS;
              return PaymentLogResponseDto.from(log, status);
            })
            .toList();
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

  private void saveRefundLog(Payment payment, int refundAmount, String paymentKey) {
    PaymentLog refundLog = PaymentLog.builder()
            .user(payment.getUser())
            .amount(-refundAmount)
            .earnedPoints(-payment.getPurchasedPoints())
            .paymentKey(paymentKey)
            .isRefund(true) // 환불 내역임을 표시
            .build();
    paymentLogRepository.save(refundLog);
  }


}
