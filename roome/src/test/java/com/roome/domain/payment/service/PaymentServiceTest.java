package com.roome.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.repository.PaymentLogRepository;
import com.roome.domain.payment.repository.PaymentRepository;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.exception.InsufficientPointsException;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @InjectMocks
  private PaymentService paymentService;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PaymentLogRepository paymentLogRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PointRepository pointRepository;

  @Mock
  private TossPaymentClient tossPaymentClient;

  @Mock
  private PointService pointService;

  @Mock
  private PointHistoryRepository pointHistoryRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id(1L)
        .nickname("testUser")
        .build();
  }

  @Test
  @DisplayName("정상적인 금액-포인트 쌍으로 결제 요청 시 카탈로그 값으로 저장되어야 한다.")
  void requestPayment_Success() {
    // given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    PaymentRequestDto requestDto = new PaymentRequestDto("order123", 5_000, 550);

    // when
    PaymentResponseDto response = paymentService.requestPayment(1L, requestDto);

    // then
    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(captor.capture());
    Payment saved = captor.getValue();

    assertThat(saved.getAmount()).isEqualTo(5_000);
    assertThat(saved.getPurchasedPoints()).isEqualTo(550);
    assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
    assertThat(response.getOrderId()).isEqualTo("order123");
    assertThat(response.getPurchasedPoints()).isEqualTo(550);
  }

  @Test
  @DisplayName("클라이언트가 포인트 수량을 위변조해도 서버 카탈로그 값으로 저장되어야 한다.")
  void requestPayment_TamperedPoints_DerivedFromCatalog() {
    // given: 1,000원을 내고 4,000포인트를 요구하는 조작된 요청
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1_000, 4_000);

    // when
    PaymentResponseDto response = paymentService.requestPayment(1L, requestDto);

    // then: 1,000원 상품의 정가 포인트(100)로 저장·응답된다
    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).save(captor.capture());

    assertThat(captor.getValue().getPurchasedPoints()).isEqualTo(100);
    assertThat(response.getPurchasedPoints()).isEqualTo(100);
  }

  @Test
  @DisplayName("판매하지 않는 금액으로 결제 요청 시 예외가 발생해야 한다.")
  void requestPayment_InvalidAmount() {
    // given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1_234, 100);

    // when & then
    assertThatThrownBy(() -> paymentService.requestPayment(1L, requestDto))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.INVALID_PAYMENT_AMOUNT.getMessage());
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  @DisplayName("존재하지 않는 사용자로 결제 요청 시 예외가 발생해야 한다.")
  void requestPayment_UserNotFound() {
    // given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1_000, 100);

    // when & then
    assertThatThrownBy(() -> paymentService.requestPayment(1L, requestDto))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("구매 후 포인트를 사용했다면 Toss 취소 요청 전에 환불이 차단되어야 한다.")
  void cancelPayment_UsedPoints_BlockedBeforeTossCall() {
    // given
    Payment payment = successPayment("pk123");
    PointHistory lastPurchase =
        new PointHistory(testUser, 550, PointReason.POINT_PURCHASE_550,
            LocalDateTime.now().minusDays(1));

    when(paymentRepository.findByPaymentKey("pk123")).thenReturn(Optional.of(payment));
    when(pointHistoryRepository.findLatestPurchase(eq(1L), anyList(), any(Pageable.class)))
        .thenReturn(List.of(lastPurchase));
    when(pointHistoryRepository.hasUsedPointsAfter(eq(1L), eq(lastPurchase.getCreatedAt()),
        anyList())).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> paymentService.cancelPayment(1L, "pk123", "단순 변심", 5_000))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PAYMENT_ALREADY_USED.getMessage());

    // 비가역적인 외부 취소 요청은 절대 나가면 안 된다
    verify(tossPaymentClient, never()).cancelPayment(any(), any(), any());
    verify(pointService, never()).usePoints(any(), any());
  }

  @Test
  @DisplayName("구매 후 포인트 사용 이력이 없으면 환불이 정상 진행되어야 한다.")
  void cancelPayment_NoUsage_Success() {
    // given
    Payment payment = successPayment("pk123");
    PointHistory lastPurchase =
        new PointHistory(testUser, 550, PointReason.POINT_PURCHASE_550,
            LocalDateTime.now().minusDays(1));

    when(paymentRepository.findByPaymentKey("pk123")).thenReturn(Optional.of(payment));
    when(pointHistoryRepository.findLatestPurchase(eq(1L), anyList(), any(Pageable.class)))
        .thenReturn(List.of(lastPurchase));
    when(pointHistoryRepository.hasUsedPointsAfter(eq(1L), eq(lastPurchase.getCreatedAt()),
        anyList())).thenReturn(false);
    when(tossPaymentClient.cancelPayment("pk123", "단순 변심", 5_000)).thenReturn(true);

    // when
    PaymentResponseDto response = paymentService.cancelPayment(1L, "pk123", "단순 변심", 5_000);

    // then
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    verify(paymentLogRepository).save(any());

    // 포인트 차감(내부, 롤백 가능)이 Toss 취소(외부, 비가역)보다 먼저 수행되어야 한다
    InOrder inOrder = inOrder(pointService, tossPaymentClient);
    inOrder.verify(pointService).usePoints(testUser, PointReason.POINT_REFUND_550);
    inOrder.verify(tossPaymentClient).cancelPayment("pk123", "단순 변심", 5_000);
  }

  @Test
  @DisplayName("포인트 잔액이 부족하면 Toss 취소 요청 전에 환불이 중단되어야 한다.")
  void cancelPayment_InsufficientPoints_BlockedBeforeTossCall() {
    // given
    Payment payment = successPayment("pk123");
    PointHistory lastPurchase =
        new PointHistory(testUser, 550, PointReason.POINT_PURCHASE_550,
            LocalDateTime.now().minusDays(1));

    when(paymentRepository.findByPaymentKey("pk123")).thenReturn(Optional.of(payment));
    when(pointHistoryRepository.findLatestPurchase(eq(1L), anyList(), any(Pageable.class)))
        .thenReturn(List.of(lastPurchase));
    when(pointHistoryRepository.hasUsedPointsAfter(eq(1L), eq(lastPurchase.getCreatedAt()),
        anyList())).thenReturn(false);
    doThrow(new InsufficientPointsException())
        .when(pointService).usePoints(testUser, PointReason.POINT_REFUND_550);

    // when & then
    assertThatThrownBy(() -> paymentService.cancelPayment(1L, "pk123", "단순 변심", 5_000))
        .isInstanceOf(InsufficientPointsException.class);

    // 비가역적인 외부 취소 요청은 절대 나가면 안 된다
    verify(tossPaymentClient, never()).cancelPayment(any(), any(), any());
    verify(paymentLogRepository, never()).save(any());
  }

  @Test
  @DisplayName("Toss 취소가 실패하면 예외가 발생해야 한다 (트랜잭션 롤백으로 내부 변경 원복).")
  void cancelPayment_TossCancelFails_ThrowsException() {
    // given
    Payment payment = successPayment("pk123");
    PointHistory lastPurchase =
        new PointHistory(testUser, 550, PointReason.POINT_PURCHASE_550,
            LocalDateTime.now().minusDays(1));

    when(paymentRepository.findByPaymentKey("pk123")).thenReturn(Optional.of(payment));
    when(pointHistoryRepository.findLatestPurchase(eq(1L), anyList(), any(Pageable.class)))
        .thenReturn(List.of(lastPurchase));
    when(pointHistoryRepository.hasUsedPointsAfter(eq(1L), eq(lastPurchase.getCreatedAt()),
        anyList())).thenReturn(false);
    when(tossPaymentClient.cancelPayment("pk123", "단순 변심", 5_000)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> paymentService.cancelPayment(1L, "pk123", "단순 변심", 5_000))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PAYMENT_CANCEL_FAILED.getMessage());
  }

  @Test
  @DisplayName("결제 완결 시 상태 변경, paymentKey 저장, 포인트 지급, 로그 저장이 수행되어야 한다.")
  void completePayment_Success() {
    // given
    Payment payment = Payment.builder()
        .user(testUser)
        .orderId("order123")
        .amount(5_000)
        .purchasedPoints(550)
        .status(PaymentStatus.PENDING)
        .build();

    // when
    paymentService.completePayment(payment, "pk123");

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    assertThat(payment.getPaymentKey()).isEqualTo("pk123");
    verify(pointService).earnPoints(testUser, PointReason.POINT_PURCHASE_550);
    verify(paymentLogRepository).save(any());
  }

  @Test
  @DisplayName("이미 완결된 결제를 다시 완결해도 포인트가 중복 지급되지 않아야 한다 (멱등성).")
  void completePayment_AlreadyCompleted_NoDoubleEarn() {
    // given
    Payment payment = successPayment("pk123");

    // when
    paymentService.completePayment(payment, "pk123");

    // then
    verify(pointService, never()).earnPoints(any(), any());
    verify(paymentLogRepository, never()).save(any());
  }

  @Test
  @DisplayName("타인의 결제를 검증 요청하면 Toss 승인 호출 전에 차단되어야 한다.")
  void verifyPayment_NotOwner_AccessDenied() {
    // given: 결제 소유자는 user 2, 요청자는 user 1
    User other = User.builder().id(2L).nickname("other").build();
    Payment payment = Payment.builder()
        .user(other)
        .orderId("order123")
        .amount(1_000)
        .purchasedPoints(100)
        .status(PaymentStatus.PENDING)
        .build();
    when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(payment));

    // when & then
    assertThatThrownBy(
        () -> paymentService.verifyPayment(1L, new PaymentVerifyDto("pk123", "order123", 1_000)))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PAYMENT_ACCESS_DENIED.getMessage());

    // 실결제 승인(부수효과)은 절대 나가면 안 된다
    verify(tossPaymentClient, never()).requestConfirm(any());
  }

  @Test
  @DisplayName("타인의 결제를 실패 처리하려 하면 차단되어야 한다.")
  void failPayment_NotOwner_AccessDenied() {
    // given
    User other = User.builder().id(2L).nickname("other").build();
    Payment payment = Payment.builder()
        .user(other).orderId("order123").amount(1_000).purchasedPoints(100)
        .status(PaymentStatus.PENDING).build();
    when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(payment));

    // when & then
    assertThatThrownBy(() -> paymentService.failPayment(1L, "order123"))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PAYMENT_ACCESS_DENIED.getMessage());
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
  }

  @Test
  @DisplayName("본인의 PENDING 결제는 실패 처리되어야 한다.")
  void failPayment_Pending_MarksFailed() {
    // given
    Payment payment = Payment.builder()
        .user(testUser).orderId("order123").amount(1_000).purchasedPoints(100)
        .status(PaymentStatus.PENDING).build();
    when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(payment));

    // when
    paymentService.failPayment(1L, "order123");

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  @Test
  @DisplayName("승인 완료된 결제는 실패로 덮어쓸 수 없어야 한다.")
  void failPayment_AlreadySuccess_Rejected() {
    // given
    Payment payment = successPayment("pk123");
    when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(payment));

    // when & then
    assertThatThrownBy(() -> paymentService.failPayment(1L, "order123"))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PAYMENT_ALREADY_PROCESSED.getMessage());
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
  }

  @Test
  @DisplayName("이미 실패한 결제의 실패 처리 재요청은 예외 없이 무시되어야 한다 (멱등성).")
  void failPayment_AlreadyFailed_Idempotent() {
    // given
    Payment payment = Payment.builder()
        .user(testUser).orderId("order123").amount(1_000).purchasedPoints(100)
        .status(PaymentStatus.FAILED).build();
    when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(payment));

    // when
    paymentService.failPayment(1L, "order123");

    // then
    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
  }

  private Payment successPayment(String paymentKey) {
    return Payment.builder()
        .user(testUser)
        .orderId("order123")
        .paymentKey(paymentKey)
        .amount(5_000)
        .purchasedPoints(550)
        .status(PaymentStatus.SUCCESS)
        .build();
  }
}
