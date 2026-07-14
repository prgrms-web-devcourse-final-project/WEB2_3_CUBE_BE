package com.roome.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.payment.dto.TossPaymentInfo;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PaymentService paymentService;

  @Mock
  private TossPaymentClient tossPaymentClient;

  private PaymentReconciliationService reconciliationService;

  @BeforeEach
  void setUp() {
    // 실제 TransactionTemplate + mock 트랜잭션 매니저로 콜백이 실제 실행되도록 구성
    TransactionTemplate transactionTemplate =
        new TransactionTemplate(mock(PlatformTransactionManager.class));
    reconciliationService = new PaymentReconciliationService(
        paymentRepository, paymentService, tossPaymentClient, transactionTemplate);
  }

  private Payment pendingPayment(Long id, String orderId) {
    return Payment.builder()
        .id(id)
        .orderId(orderId)
        .amount(5_000)
        .purchasedPoints(550)
        .status(PaymentStatus.PENDING)
        .build();
  }

  private void givenStalePayments(Payment... payments) {
    when(paymentRepository.findByStatusAndCreatedAtBefore(
        eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
        .thenReturn(List.of(payments));
  }

  @Test
  @DisplayName("Toss에서 승인(DONE)된 미완결 결제는 완결 처리되어야 한다.")
  void reconcile_TossDone_CompletesPayment() {
    // given
    Payment payment = pendingPayment(1L, "order1");
    givenStalePayments(payment);
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(tossPaymentClient.findPaymentByOrderId("order1"))
        .thenReturn(Optional.of(new TossPaymentInfo("pk1", "DONE", 5_000)));

    // when
    reconciliationService.reconcilePendingPayments();

    // then
    verify(paymentService).completePayment(payment, "pk1");
  }

  @Test
  @DisplayName("승인 금액이 저장된 금액과 다르면 자동 완결하지 않고 PENDING으로 남겨야 한다.")
  void reconcile_AmountMismatch_LeftForManualReview() {
    // given
    Payment payment = pendingPayment(1L, "order1");
    givenStalePayments(payment);
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(tossPaymentClient.findPaymentByOrderId("order1"))
        .thenReturn(Optional.of(new TossPaymentInfo("pk1", "DONE", 30_000)));

    // when
    reconciliationService.reconcilePendingPayments();

    // then
    verify(paymentService, never()).completePayment(any(), any());
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("Toss에 결제 기록이 없으면 실패 처리되어야 한다.")
  void reconcile_NotFoundAtToss_MarksFailed() {
    // given
    Payment payment = pendingPayment(1L, "order1");
    givenStalePayments(payment);
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(tossPaymentClient.findPaymentByOrderId("order1")).thenReturn(Optional.empty());

    // when
    reconciliationService.reconcilePendingPayments();

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    verify(paymentService, never()).completePayment(any(), any());
  }

  @Test
  @DisplayName("Toss에서 취소된 결제는 취소 상태로 동기화되어야 한다.")
  void reconcile_TossCanceled_MarksCanceled() {
    // given
    Payment payment = pendingPayment(1L, "order1");
    givenStalePayments(payment);
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
    when(tossPaymentClient.findPaymentByOrderId("order1"))
        .thenReturn(Optional.of(new TossPaymentInfo("pk1", "CANCELED", 5_000)));

    // when
    reconciliationService.reconcilePendingPayments();

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
  }

  @Test
  @DisplayName("트랜잭션 내 재확인 시 이미 완결된 결제는 건너뛰어야 한다 (멱등성).")
  void reconcile_AlreadyCompleted_Skipped() {
    // given: 대사 시작 후 사용자 verify가 먼저 완결한 상황
    Payment stale = pendingPayment(1L, "order1");
    givenStalePayments(stale);
    Payment completed = Payment.builder()
        .id(1L).orderId("order1").amount(5_000).purchasedPoints(550)
        .status(PaymentStatus.SUCCESS).build();
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(completed));
    when(tossPaymentClient.findPaymentByOrderId("order1"))
        .thenReturn(Optional.of(new TossPaymentInfo("pk1", "DONE", 5_000)));

    // when
    reconciliationService.reconcilePendingPayments();

    // then
    verify(paymentService, never()).completePayment(any(), any());
    assertThat(completed.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
  }

  @Test
  @DisplayName("한 건의 대사 실패가 다른 건의 대사를 막지 않아야 한다.")
  void reconcile_OneFailure_DoesNotBlockOthers() {
    // given: 첫 건은 Toss 조회 실패, 두 번째 건은 정상 승인
    Payment first = pendingPayment(1L, "order1");
    Payment second = pendingPayment(2L, "order2");
    givenStalePayments(first, second);
    when(tossPaymentClient.findPaymentByOrderId("order1"))
        .thenThrow(new RuntimeException("Toss 조회 실패"));
    when(tossPaymentClient.findPaymentByOrderId("order2"))
        .thenReturn(Optional.of(new TossPaymentInfo("pk2", "DONE", 5_000)));
    when(paymentRepository.findById(2L)).thenReturn(Optional.of(second));

    // when
    reconciliationService.reconcilePendingPayments();

    // then
    verify(paymentService).completePayment(second, "pk2");
  }
}
