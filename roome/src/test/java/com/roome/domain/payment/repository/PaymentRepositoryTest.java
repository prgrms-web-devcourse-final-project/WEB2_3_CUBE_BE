package com.roome.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.roome.domain.config.TestQueryDslConfig;
import com.roome.domain.payment.entity.Payment;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import({TestQueryDslConfig.class, JpaConfig.class})
@DataJpaTest
class PaymentRepositoryTest {

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  @DisplayName("임계 시점 이전에 생성된 PENDING 결제만 대사 대상으로 조회되어야 한다.")
  void findByStatusAndCreatedAtBefore() {
    // given
    User user = userRepository.save(createUser());

    Payment stalePending = paymentRepository.save(payment(user, "order-stale", PaymentStatus.PENDING));
    Payment freshPending = paymentRepository.save(payment(user, "order-fresh", PaymentStatus.PENDING));
    Payment staleSuccess = paymentRepository.save(payment(user, "order-done", PaymentStatus.SUCCESS));

    // auditing이 찍은 createdAt을 과거로 되돌린다 (bulk update는 updatable=false를 우회)
    backdate(stalePending.getId(), LocalDateTime.now().minusHours(1));
    backdate(staleSuccess.getId(), LocalDateTime.now().minusHours(1));

    // when
    List<Payment> result = paymentRepository.findByStatusAndCreatedAtBefore(
        PaymentStatus.PENDING, LocalDateTime.now().minusMinutes(30));

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getOrderId()).isEqualTo("order-stale");
  }

  @Test
  @DisplayName("동시 수정 시 낙관적 락(@Version)이 stale 엔티티의 변경을 거부해야 한다.")
  void optimisticLock_RejectsStaleUpdate() {
    // given: 저장된 결제를 두 영속성 컨텍스트가 각각 로드한 상황을 시뮬레이션
    User user = userRepository.save(createUser());
    Payment saved = paymentRepository.save(payment(user, "order-lock", PaymentStatus.PENDING));
    entityManager.flush();
    entityManager.clear();

    Payment loadedA = paymentRepository.findById(saved.getId()).orElseThrow();
    entityManager.detach(loadedA); // A는 version=0 스냅샷을 들고 분리됨

    // 다른 요청(B)이 먼저 상태를 변경하고 커밋 -> version 0 -> 1
    Payment loadedB = paymentRepository.findById(saved.getId()).orElseThrow();
    loadedB.markApproved("pk-b", LocalDateTime.now());
    paymentRepository.saveAndFlush(loadedB);
    entityManager.clear();

    // when & then: 뒤늦게 stale한 A(version=0)로 변경을 시도하면 충돌
    loadedA.markCanceled(LocalDateTime.now());
    assertThatThrownBy(() -> paymentRepository.saveAndFlush(loadedA))
        .isInstanceOf(OptimisticLockingFailureException.class);
  }

  private void backdate(Long paymentId, LocalDateTime createdAt) {
    entityManager.flush();
    entityManager.createQuery("UPDATE Payment p SET p.createdAt = :createdAt WHERE p.id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", paymentId)
        .executeUpdate();
    entityManager.clear();
  }

  private Payment payment(User user, String orderId, PaymentStatus status) {
    return Payment.builder()
        .user(user)
        .orderId(orderId)
        .amount(5_000)
        .purchasedPoints(550)
        .status(status)
        .build();
  }

  private User createUser() {
    return User.builder()
        .email("user@gmail.com")
        .name("user")
        .nickname("nickname")
        .profileImage("profile")
        .provider(Provider.GOOGLE)
        .providerId("provId")
        .status(Status.ONLINE)
        .lastLogin(LocalDateTime.of(2025, 1, 1, 1, 1))
        .refreshToken("refToken")
        .build();
  }
}
