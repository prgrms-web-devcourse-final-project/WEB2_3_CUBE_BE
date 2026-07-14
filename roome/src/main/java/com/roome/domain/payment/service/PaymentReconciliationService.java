package com.roome.domain.payment.service;

import com.roome.domain.payment.dto.TossPaymentInfo;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.repository.PaymentRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

// 결제 대사(reconciliation) 배치
// 사용자 콜백(/verify)이 유실되어 PENDING에 방치된 결제를 Toss 조회 API로 대조하여
// "돈은 빠졌는데 포인트는 없는" 불일치를 발견·복구한다. 진실의 원천은 항상 Toss 측 상태다.
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

  // 이 시간 이상 PENDING에 머문 결제만 대사 대상으로 삼는다 (진행 중인 결제와의 경합 방지)
  private static final Duration STALE_THRESHOLD = Duration.ofMinutes(30);

  private final PaymentRepository paymentRepository;
  private final PaymentService paymentService;
  private final TossPaymentClient tossPaymentClient;
  private final TransactionTemplate transactionTemplate;

  @Scheduled(fixedDelay = 600_000, initialDelay = 60_000) // 10분 주기
  public void reconcilePendingPayments() {
    LocalDateTime threshold = LocalDateTime.now().minus(STALE_THRESHOLD);
    List<Payment> stalePayments =
        paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold);

    if (stalePayments.isEmpty()) {
      return;
    }
    log.info("결제 대사 시작: 대상 {}건", stalePayments.size());

    // 한 건의 실패가 나머지 건의 대사를 막지 않도록 건별로 격리한다
    for (Payment stale : stalePayments) {
      try {
        reconcile(stale.getId(), stale.getOrderId());
      } catch (Exception e) {
        log.error("결제 대사 실패 - 다음 주기에 재시도: paymentId={}, orderId={}",
            stale.getId(), stale.getOrderId(), e);
      }
    }
  }

  private void reconcile(Long paymentId, String orderId) {
    // 외부 조회는 트랜잭션 밖에서 수행한다 (커넥션을 점유한 채 네트워크 대기 금지)
    Optional<TossPaymentInfo> tossInfo = tossPaymentClient.findPaymentByOrderId(orderId);

    transactionTemplate.executeWithoutResult(tx -> {
      // 트랜잭션 안에서 다시 조회하여 상태를 재확인한다 (그 사이 verify가 완결했을 수 있음 - 멱등성)
      Payment payment = paymentRepository.findById(paymentId).orElse(null);
      if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
        return;
      }

      // Toss에 기록 자체가 없음 = 사용자가 결제창까지 도달하지 못함 → 실패 처리
      if (tossInfo.isEmpty()) {
        payment.updateStatus(PaymentStatus.FAILED);
        log.info("[대사] Toss 기록 없음, 실패 처리: orderId={}", orderId);
        return;
      }

      TossPaymentInfo info = tossInfo.get();
      switch (info.getStatus()) {
        case "DONE" -> {
          // 돈은 빠졌는데 우리 쪽 완결이 유실된 케이스 - 대사의 존재 이유
          if (info.getTotalAmount() != payment.getAmount()) {
            // 금액이 어긋난 승인은 자동 처리 금지 영역 - 수동 확인 대상으로 남긴다
            log.error("[대사 경고] 승인 금액 불일치, 수동 확인 필요: orderId={}, 저장 금액={}, Toss 금액={}",
                orderId, payment.getAmount(), info.getTotalAmount());
            return;
          }
          paymentService.completePayment(payment, info.getPaymentKey(), info.getApprovedAt());
          log.warn("[대사 복구] 승인됐지만 미완결이던 결제를 완결: orderId={}, amount={}, points={}",
              orderId, payment.getAmount(), payment.getPurchasedPoints());
        }
        case "CANCELED", "PARTIAL_CANCELED" -> {
          payment.updateStatus(PaymentStatus.CANCELED);
          log.info("[대사] Toss에서 취소 확인, 취소 처리: orderId={}", orderId);
        }
        case "WAITING_FOR_DEPOSIT" -> {
          // 가상계좌 입금 대기: 아직 결과가 정해지지 않았으므로 PENDING 유지
          log.info("[대사] 입금 대기 중, 유지: orderId={}", orderId);
        }
        default -> {
          // READY, IN_PROGRESS, EXPIRED, ABORTED 등: 임계 시간이 지나도록 승인에 도달하지 못함
          payment.updateStatus(PaymentStatus.FAILED);
          log.info("[대사] 미완료 상태({}), 실패 처리: orderId={}", info.getStatus(), orderId);
        }
      }
    });
  }
}
