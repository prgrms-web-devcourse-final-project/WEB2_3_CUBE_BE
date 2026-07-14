package com.roome.domain.point.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.roome.domain.config.TestQueryDslConfig;
import com.roome.domain.point.entity.Point;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import({TestQueryDslConfig.class, JpaConfig.class})
@DataJpaTest
class PointRepositoryTest {

  @Autowired
  private PointRepository pointRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  private Long userId;

  @BeforeEach
  void setUp() {
    User user = userRepository.save(createUser());
    userId = user.getId();
    pointRepository.save(new Point(user, 1_000, 1_000, 0));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("addBalance는 잔액과 누적 적립을 원자적으로 증가시킨다.")
  void addBalance_Increments() {
    // when
    int updated = pointRepository.addBalance(userId, 500, LocalDateTime.now());
    entityManager.clear();

    // then
    Point point = pointRepository.findByUserId(userId).orElseThrow();
    assertThat(updated).isEqualTo(1);
    assertThat(point.getBalance()).isEqualTo(1_500);
    assertThat(point.getTotalEarned()).isEqualTo(1_500);
  }

  @Test
  @DisplayName("잔액이 충분하면 subtractBalanceIfEnough가 차감하고 1을 반환한다.")
  void subtractBalanceIfEnough_SufficientBalance() {
    // when
    int updated = pointRepository.subtractBalanceIfEnough(userId, 400, LocalDateTime.now());
    entityManager.clear();

    // then
    Point point = pointRepository.findByUserId(userId).orElseThrow();
    assertThat(updated).isEqualTo(1);
    assertThat(point.getBalance()).isEqualTo(600);
    assertThat(point.getTotalUsed()).isEqualTo(400);
  }

  @Test
  @DisplayName("잔액이 부족하면 subtractBalanceIfEnough가 0을 반환하고 잔액을 바꾸지 않는다.")
  void subtractBalanceIfEnough_InsufficientBalance() {
    // when: 보유 1,000 < 요청 1,500
    int updated = pointRepository.subtractBalanceIfEnough(userId, 1_500, LocalDateTime.now());
    entityManager.clear();

    // then
    Point point = pointRepository.findByUserId(userId).orElseThrow();
    assertThat(updated).isZero();
    assertThat(point.getBalance()).isEqualTo(1_000);
    assertThat(point.getTotalUsed()).isZero();
  }

  @Test
  @DisplayName("잔액과 정확히 같은 금액은 차감 가능하다 (경계값).")
  void subtractBalanceIfEnough_ExactBalance() {
    // when
    int updated = pointRepository.subtractBalanceIfEnough(userId, 1_000, LocalDateTime.now());
    entityManager.clear();

    // then
    Point point = pointRepository.findByUserId(userId).orElseThrow();
    assertThat(updated).isEqualTo(1);
    assertThat(point.getBalance()).isZero();
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
