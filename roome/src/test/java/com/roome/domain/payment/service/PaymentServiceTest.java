package com.roome.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.repository.PaymentLogRepository;
import com.roome.domain.payment.repository.PaymentRepository;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
}
