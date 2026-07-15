package com.roome.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.payment.dto.PaymentLogResponseDto;
import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.dto.TossPaymentInfo;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.entity.PointProduct;
import com.roome.domain.payment.repository.PaymentLogRepository;
import com.roome.domain.payment.repository.PaymentRepository;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentLogRepository paymentLogRepository;
  private final UserRepository userRepository;
  private final TossPaymentClient tossPaymentClient;
  private final PointService pointService;
  private final PointHistoryRepository pointHistoryRepository;

  private static final int MAX_PAGE_SIZE = 100; // 결제 내역 조회 페이지 크기 상한 (과도한 조회 방지)

  // 사용자가 포인트 결제를 요청하면 DB에 저장해 줌
  // 결제 진행 중인 상태
  @Transactional
  public PaymentResponseDto requestPayment(Long userId, PaymentRequestDto requestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 중복 주문 ID 선제 확인 - 최종 방어는 DB unique 제약이며, 아래 save의 catch가 레이스 케이스를 처리
    if (paymentRepository.existsByOrderId(requestDto.getOrderId())) {
      throw new BusinessException(ErrorCode.ORDER_ID_ALREADY_EXISTS);
    }

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

    try {
      paymentRepository.save(payment);
    } catch (DataIntegrityViolationException e) {
      // exists 확인과 save 사이에 같은 orderId가 먼저 저장된 경우 (TOCTOU 레이스) - unique 제약이 최종 방어
      throw new BusinessException(ErrorCode.ORDER_ID_ALREADY_EXISTS);
    }
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
    log.info("결제 검증 시작: orderId={}, userId={}", verifyDto.getOrderId(), userId);

    Payment payment = paymentRepository.findByOrderId(verifyDto.getOrderId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    // 소유권 검증: 본인의 결제만 검증 요청 가능 (Toss 승인 호출 등 어떤 부수효과보다 먼저 수행)
    if (!payment.getUser().getId().equals(userId)) {
      log.warn("결제 검증 권한 없음: orderId={}, 요청자={}, 소유자={}",
          verifyDto.getOrderId(), userId, payment.getUser().getId());
      throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
    }

    // 멱등성: 이미 완결된 결제면 Toss 재승인, 재지급 없이 기존 결과를 그대로 반환
    // (더블클릭이나 네트워크 재시도로 같은 orderId가 재요청되는 경우)
    if (payment.getStatus() == PaymentStatus.SUCCESS) {
      log.info("이미 완결된 결제 - 멱등 응답 반환: orderId={}", verifyDto.getOrderId());
      return toResponse(payment);
    }

    // PENDING이 아닌 상태(FAILED/CANCELED)는 검증할 수 없다
    if (payment.getStatus() != PaymentStatus.PENDING) {
      throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
    }

    if (payment.getAmount() != verifyDto.getAmount()) {
      log.warn("결제 금액 불일치: orderId={}, 요청 금액={}, 저장 금액={}",
          verifyDto.getOrderId(), verifyDto.getAmount(), payment.getAmount());
      throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }

    TossPaymentInfo tossInfo;
    try {
      // 응답 전문에는 카드 및 구매자 정보가 포함될 수 있어 body를 로깅 x
      ResponseEntity<String> response = tossPaymentClient.requestConfirm(verifyDto);
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
      }

      // confirm 응답을 파싱해 상태, 금액 검증 및 원장 메타데이터에 사용
      tossInfo = TossPaymentClient.parsePaymentInfo(new ObjectMapper().readTree(response.getBody()));
      if (!"DONE".equals(tossInfo.getStatus())) {
        log.warn("결제 상태가 DONE이 아님: orderId={}, status={}",
            verifyDto.getOrderId(), tossInfo.getStatus());
        throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
      }
      if (tossInfo.getTotalAmount() != payment.getAmount()) {
        log.warn("승인 금액 불일치: orderId={}, 저장 금액={}, Toss 승인 금액={}",
            verifyDto.getOrderId(), payment.getAmount(), tossInfo.getTotalAmount());
        throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
      }
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("결제 승인 응답 처리 중 오류: orderId={}", verifyDto.getOrderId(), e);
      throw new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
    }

    // 결제 완결 처리 (상태 변경 + 포인트 지급 + 로그 저장)
    completePayment(payment, verifyDto.getPaymentKey(), tossInfo);

    log.info("결제 성공 및 포인트 지급 완료: orderId={}, userId={}, pointsAdded={}",
        verifyDto.getOrderId(), userId, payment.getPurchasedPoints());

    return toResponse(payment);
  }

  private PaymentResponseDto toResponse(Payment payment) {
    return PaymentResponseDto.builder()
        .orderId(payment.getOrderId())
        .paymentKey(payment.getPaymentKey())
        .amount(payment.getAmount())
        .purchasedPoints(payment.getPurchasedPoints())
        .status(payment.getStatus())
        .build();
  }

  // 승인이 확인된 결제를 완결 처리한다 (상태 변경 + 포인트 지급 + 로그 저장)
  // verifyPayment(사용자 콜백)와 PaymentReconciliationService(대사 배치)가 공유하는 단일 완결 경로
  @Transactional
  public void completePayment(Payment payment, String paymentKey, TossPaymentInfo tossInfo) {
    if (payment.getStatus() == PaymentStatus.SUCCESS) {
      log.warn("이미 완결된 결제 - 중복 완결 방지: orderId={}", payment.getOrderId());
      return;
    }

    // 승인 시각은 Toss가 확정한 값을 원장에 기록하고, 값이 없으면 서버 시각으로 대체
    LocalDateTime approvedAt = tossInfo.getApprovedAt() != null
        ? tossInfo.getApprovedAt() : LocalDateTime.now();
    payment.markApproved(paymentKey, approvedAt);
    // PG 승인 메타데이터를 원장에 기록
    payment.applyPgDetails(tossInfo.getMethod(), tossInfo.getReceiptUrl(), tossInfo.getApproveNo());

    // Toss가 승인한 결제 금액(payment.amount)을 기준으로 카탈로그에서 지급 사유를 파생
    PointProduct product = PointProduct.findByPrice(payment.getAmount())
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT));
    pointService.earnPoints(payment.getUser(), product.getEarnReason());

    savePaymentLog(payment, paymentKey);
  }

  // 결제 실패 처리
  @Transactional
  public void failPayment(Long userId, String orderId) {
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

    // 소유권 검증: 본인의 결제만 실패 처리 가능
    if (!payment.getUser().getId().equals(userId)) {
      log.warn("결제 실패 처리 권한 없음: orderId={}, 요청자={}, 소유자={}",
          orderId, userId, payment.getUser().getId());
      throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
    }

    // 이미 실패 처리된 결제는 무시 (실패 콜백 재시도에 대한 멱등 처리)
    if (payment.getStatus() == PaymentStatus.FAILED) {
      return;
    }

    // PENDING 상태만 실패로 전이 가능 (승인이나 취소된 결제를 실패로 덮어쓰는 것 방지)
    if (payment.getStatus() != PaymentStatus.PENDING) {
      throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
    }

    payment.markFailed();

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

    // 결제 상태가 SUCCESS가 아닐 경우, 취소 불가능
    if (!payment.getStatus().equals(PaymentStatus.SUCCESS)) {
      throw new BusinessException(ErrorCode.PAYMENT_NOT_CANCELABLE);
    }

    // 환불 가능 기간 체크: '최신 구매'가 아니라 '이 결제'의 승인 시각을 기준으로 판정 (건별 검증)
    LocalDateTime approvedAt = payment.getApprovedAt();
    if (approvedAt == null || approvedAt.isBefore(LocalDateTime.now().minusDays(7))) {
      throw new BusinessException(ErrorCode.PAYMENT_REFUND_PERIOD_EXCEEDED);
    }

    // 포인트 사용 여부 체크: 이 결제의 승인 이후 포인트를 사용했다면 환불 불가
    boolean hasUsedPoints = pointHistoryRepository.hasUsedPointsAfter(
        userId, approvedAt, PointProduct.refundReasons());
    if (hasUsedPoints) {
      throw new BusinessException(ErrorCode.PAYMENT_ALREADY_USED);
    }

    // 부분 취소는 지원하지 않으므로 cancelAmount가 주어졌다면 결제 전액과 일치해야 함
    if (cancelAmount != null && cancelAmount != payment.getAmount()) {
      throw new BusinessException(ErrorCode.PARTIAL_CANCEL_NOT_SUPPORTED);
    }

    // 환불 금액과 차감 포인트는 클라이언트 값이 아니라 이 결제에서 파생
    int refundAmount = payment.getAmount();
    PointProduct refundProduct = PointProduct.findByPrice(refundAmount)
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT));
    int refundPoints = refundProduct.getPoints();

    // 실패 가능한 내부 변경(포인트 차감, 상태 변경, 로그)을 모두 끝낸 뒤 Toss 취소 실행
    // Toss 취소가 실패하면 예외로 트랜잭션 전체가 롤백돼서 내부 변경이 자동 원상복구됨

    // 사용자 포인트 차감 (잔액 부족 시 여기서 예외가 나고 Toss 호출 전에 중단)
    pointService.usePoints(payment.getUser(), refundProduct.getRefundReason());

    // 결제 상태 업데이트
    payment.markCanceled(LocalDateTime.now());

    saveRefundLog(payment, refundAmount, paymentKey);

    // Toss API에 결제 취소 요청 (전액 취소)
    boolean isCanceled = tossPaymentClient.cancelPayment(payment.getPaymentKey(), cancelReason,
        refundAmount);
    if (!isCanceled) {
      throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    log.info("결제 취소 완료: paymentKey={}, userId={}, refundPoints={}, refundAmount={}",
            paymentKey, userId, refundPoints, refundAmount);

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

    // page는 1부터 시작
    // 잘못된 입력으로 PageRequest.of(-1, ...)가 던지는 500을 방지하고,
    // size는 상한(MAX_PAGE_SIZE)으로 clamp하여 과도한 조회를 막음
    int safePage = Math.max(1, page) - 1;
    int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

    PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<PaymentLog> paymentLogs = paymentLogRepository.findByUserWithPayment(user, pageRequest);

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
            .payment(payment)
            .amount(payment.getAmount())
            .earnedPoints(payment.getPurchasedPoints())
            .paymentKey(paymentKey)
            .build();
    paymentLogRepository.save(paymentLog);
  }

  private void saveRefundLog(Payment payment, int refundAmount, String paymentKey) {
    PaymentLog refundLog = PaymentLog.builder()
            .user(payment.getUser())
            .payment(payment)
            .amount(-refundAmount)
            .earnedPoints(-payment.getPurchasedPoints())
            .paymentKey(paymentKey)
            .isRefund(true) // 환불 내역임을 표시
            .build();
    paymentLogRepository.save(refundLog);
  }


}
