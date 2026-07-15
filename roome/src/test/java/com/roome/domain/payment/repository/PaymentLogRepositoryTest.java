package com.roome.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.roome.domain.config.TestQueryDslConfig;
import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import({TestQueryDslConfig.class, JpaConfig.class})
@DataJpaTest
class PaymentLogRepositoryTest {

  @Autowired
  private PaymentLogRepository paymentLogRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  @DisplayName("결제 로그는 연결된 Payment를 통해 주문 건과 매칭할 수 있다.")
  void paymentLog_LinkedToPayment() {
    // given
    User user = userRepository.save(createUser("user@gmail.com", "provId-1"));
    Payment payment = paymentRepository.save(payment(user, "order-1"));
    paymentLogRepository.save(PaymentLog.builder()
        .user(user).payment(payment)
        .amount(5_000).earnedPoints(550).paymentKey("pk1")
        .build());
    entityManager.flush();
    entityManager.clear();

    // when
    Page<PaymentLog> result = paymentLogRepository.findByUserWithPayment(
        user, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

    // then: 로그에서 연결된 결제 건의 orderId를 추적할 수 있다
    List<PaymentLog> logs = result.getContent();
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getPayment()).isNotNull();
    assertThat(logs.get(0).getPayment().getOrderId()).isEqualTo("order-1");
  }

  @Test
  @DisplayName("payment가 연결되지 않은 기존 로그도 조회에서 누락되지 않는다 (LEFT JOIN).")
  void legacyLog_WithoutPayment_StillReturned() {
    // given: payment 연결이 없는 레거시 로그
    User user = userRepository.save(createUser("legacy@gmail.com", "provId-2"));
    paymentLogRepository.save(PaymentLog.builder()
        .user(user).payment(null)
        .amount(1_000).earnedPoints(100).paymentKey("pk-legacy")
        .build());
    entityManager.flush();
    entityManager.clear();

    // when
    Page<PaymentLog> result = paymentLogRepository.findByUserWithPayment(
        user, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getPayment()).isNull();
  }

  private Payment payment(User user, String orderId) {
    return Payment.builder()
        .user(user)
        .orderId(orderId)
        .amount(5_000)
        .purchasedPoints(550)
        .status(PaymentStatus.SUCCESS)
        .build();
  }

  private User createUser(String email, String providerId) {
    return User.builder()
        .email(email)
        .name("user")
        .nickname("nickname")
        .profileImage("profile")
        .provider(Provider.GOOGLE)
        .providerId(providerId)
        .status(Status.ONLINE)
        .lastLogin(LocalDateTime.of(2025, 1, 1, 1, 1))
        .refreshToken("refToken")
        .build();
  }
}
