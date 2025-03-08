//package com.roome.domain.payment.service;
//
//import com.roome.domain.payment.dto.PaymentRequestDto;
//import com.roome.domain.payment.dto.PaymentResponseDto;
//import com.roome.domain.payment.dto.PaymentVerifyDto;
//import com.roome.domain.payment.entity.Payment;
//import com.roome.domain.payment.entity.PaymentStatus;
//import com.roome.domain.payment.repository.PaymentLogRepository;
//import com.roome.domain.payment.repository.PaymentRepository;
//import com.roome.domain.point.entity.Point;
//import com.roome.domain.point.repository.PointRepository;
//import com.roome.domain.user.entity.User;
//import com.roome.domain.user.repository.UserRepository;
//import com.roome.global.exception.BusinessException;
//import com.roome.global.exception.ErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@Transactional
//class PaymentServiceTest {
//
//    @InjectMocks
//    private PaymentService paymentService;
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private PaymentLogRepository paymentLogRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PointRepository pointRepository;
//
//    @Mock
//    private TossPaymentClient tossPaymentClient;
//
//    private User testUser;
//    private PaymentRequestDto requestDto;
//    private PaymentVerifyDto verifyDto;
//    private Payment testPayment;
//
//    @BeforeEach
//    void setUp() {
//        testUser = User.builder()
//                .id(1L)
//                .nickname("testUser")
//                .build();
//
//        requestDto = new PaymentRequestDto("order123", 1000, 10);
//        verifyDto = new PaymentVerifyDto("paymentKey123", "order123", 1000);
//
//        testPayment = Payment.builder()
//                .user(testUser)
//                .orderId("order123")
//                .amount(1000)
//                .purchasedPoints(10)
//                .status(PaymentStatus.PENDING)
//                .build();
//    }
//
//    @Test
//    @DisplayName("결제 요청이 정상적으로 저장되어야 한다.")
//    void requestPayment_Success() {
//        // given
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
//
//        // when
//        PaymentResponseDto response = paymentService.requestPayment(1L, requestDto);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getOrderId()).isEqualTo("order123");
//        assertThat(response.getAmount()).isEqualTo(1000);
//        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 사용자로 결제 요청 시 예외가 발생해야 한다.")
//    void requestPayment_UserNotFound() {
//        // given
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> paymentService.requestPayment(1L, requestDto))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    @DisplayName("결제 검증이 정상적으로 처리되고 포인트가 지급되어야 한다.")
//    void verifyPayment_Success() {
//        // given
//        when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(testPayment));
//        when(tossPaymentClient.verifyPayment("paymentKey123", "order123", 1000)).thenReturn(true);
//        when(pointRepository.findByUser(testUser)).thenReturn(Optional.of(new Point(testUser, 0, 0, 0)));
//
//        // when
//        PaymentResponseDto response = paymentService.verifyPayment(1L, verifyDto);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getOrderId()).isEqualTo("order123");
//        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    @Test
//    @DisplayName("결제 검증 시 금액 불일치로 인해 예외가 발생해야 한다.")
//    void verifyPayment_AmountMismatch() {
//        // given
//        when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(testPayment));
//
//        PaymentVerifyDto incorrectVerifyDto = new PaymentVerifyDto("paymentKey123", "order123", 500); // 잘못된 금액
//
//        // when & then
//        assertThatThrownBy(() -> paymentService.verifyPayment(1L, incorrectVerifyDto))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining(ErrorCode.PAYMENT_AMOUNT_MISMATCH.getMessage());
//    }
//
//    @Test
//    @DisplayName("결제 검증 시 토스 API 오류로 인해 예외가 발생해야 한다.")
//    void verifyPayment_TossApiFailure() {
//        // given
//        when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(testPayment));
//        when(tossPaymentClient.verifyPayment("paymentKey123", "order123", 1000)).thenReturn(false);
//
//        // when & then
//        assertThatThrownBy(() -> paymentService.verifyPayment(1L, verifyDto))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining(ErrorCode.PAYMENT_VERIFICATION_FAILED.getMessage());
//    }
//
//    @Test
//    @DisplayName("결제 실패 처리 시 상태가 FAILED로 업데이트되어야 한다.")
//    void failPayment_Success() {
//        // given
//        when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.of(testPayment));
//
//        // when
//        paymentService.failPayment("order123");
//
//        // then
//        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
//        verify(paymentRepository, times(1)).save(testPayment);
//    }
//
//    @Test
//    @DisplayName("결제 실패 처리 시 주문 ID가 없으면 예외가 발생해야 한다.")
//    void failPayment_OrderNotFound() {
//        // given
//        when(paymentRepository.findByOrderId("order123")).thenReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> paymentService.failPayment("order123"))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining(ErrorCode.PAYMENT_NOT_FOUND.getMessage());
//    }
//}
