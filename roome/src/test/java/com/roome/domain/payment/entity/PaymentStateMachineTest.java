package com.roome.domain.payment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.roome.domain.user.entity.User;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentStateMachineTest {

  @Nested
  @DisplayName("PaymentStatus.canTransitionTo")
  class TransitionRules {

    @Test
    @DisplayName("PENDING은 SUCCESS/FAILED/CANCELED로 전이할 수 있다.")
    void pendingTransitions() {
      assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.SUCCESS)).isTrue();
      assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.FAILED)).isTrue();
      assertThat(PaymentStatus.PENDING.canTransitionTo(PaymentStatus.CANCELED)).isTrue();
    }

    @Test
    @DisplayName("SUCCESS는 CANCELED로만 전이할 수 있다.")
    void successTransitions() {
      assertThat(PaymentStatus.SUCCESS.canTransitionTo(PaymentStatus.CANCELED)).isTrue();
      assertThat(PaymentStatus.SUCCESS.canTransitionTo(PaymentStatus.FAILED)).isFalse();
      assertThat(PaymentStatus.SUCCESS.canTransitionTo(PaymentStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName("FAILED와 CANCELED는 종료 상태로 어떤 전이도 불가능하다.")
    void terminalStates() {
      for (PaymentStatus target : PaymentStatus.values()) {
        assertThat(PaymentStatus.FAILED.canTransitionTo(target)).isFalse();
        assertThat(PaymentStatus.CANCELED.canTransitionTo(target)).isFalse();
      }
    }
  }

  @Nested
  @DisplayName("Payment 상태 전이 메서드")
  class EntityTransitions {

    @Test
    @DisplayName("PENDING 결제는 승인 완결할 수 있다.")
    void markApproved_FromPending() {
      Payment payment = pending();
      payment.markApproved("pk1", LocalDateTime.now());
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
      assertThat(payment.getPaymentKey()).isEqualTo("pk1");
    }

    @Test
    @DisplayName("SUCCESS 결제를 다시 승인하면 예외가 발생한다.")
    void markApproved_FromSuccess_Rejected() {
      Payment payment = withStatus(PaymentStatus.SUCCESS);
      assertThatThrownBy(() -> payment.markApproved("pk1", LocalDateTime.now()))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION.getMessage());
    }

    @Test
    @DisplayName("SUCCESS 결제를 실패로 덮어쓸 수 없다.")
    void markFailed_FromSuccess_Rejected() {
      Payment payment = withStatus(PaymentStatus.SUCCESS);
      assertThatThrownBy(payment::markFailed)
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION.getMessage());
    }

    @Test
    @DisplayName("SUCCESS 결제는 취소(환불)할 수 있다.")
    void markCanceled_FromSuccess() {
      Payment payment = withStatus(PaymentStatus.SUCCESS);
      payment.markCanceled(LocalDateTime.now());
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
      assertThat(payment.getCanceledAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 취소된 결제는 다시 취소할 수 없다.")
    void markCanceled_FromCanceled_Rejected() {
      Payment payment = withStatus(PaymentStatus.CANCELED);
      assertThatThrownBy(() -> payment.markCanceled(LocalDateTime.now()))
          .isInstanceOf(BusinessException.class)
          .hasMessageContaining(ErrorCode.INVALID_PAYMENT_STATUS_TRANSITION.getMessage());
    }

    private Payment pending() {
      return withStatus(PaymentStatus.PENDING);
    }

    private Payment withStatus(PaymentStatus status) {
      return Payment.builder()
          .user(User.builder().id(1L).build())
          .orderId("order1")
          .amount(5_000)
          .purchasedPoints(550)
          .status(status)
          .build();
    }
  }
}
