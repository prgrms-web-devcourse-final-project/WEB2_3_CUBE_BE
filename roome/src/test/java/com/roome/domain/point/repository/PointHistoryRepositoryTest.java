package com.roome.domain.point.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.roome.domain.config.TestQueryDslConfig;
import com.roome.domain.payment.entity.PointProduct;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestQueryDslConfig.class)
@DataJpaTest
class PointHistoryRepositoryTest {

  @Autowired
  private PointHistoryRepository pointHistoryRepository;

  @Autowired
  private UserRepository userRepository;

  private User user;
  private final LocalDateTime purchaseTime = LocalDateTime.of(2025, 3, 1, 12, 0);

  @BeforeEach
  void setUp() {
    user = userRepository.save(createUser("user@gmail.com", "user"));
    // 검증 기준점이 되는 구매 이력
    pointHistoryRepository.save(
        new PointHistory(user, 550, PointReason.POINT_PURCHASE_550, purchaseTime));
  }

  @Test
  @DisplayName("구매 이후 포인트 사용 이력이 있으면 true를 반환한다.")
  void usageAfterPurchase_ReturnsTrue() {
    pointHistoryRepository.save(
        new PointHistory(user, -400, PointReason.THEME_PURCHASE, purchaseTime.plusDays(1)));

    boolean result = pointHistoryRepository.hasUsedPointsAfter(
        user.getId(), purchaseTime, PointProduct.refundReasons());

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("구매 이후 사용 이력이 없으면 false를 반환한다.")
  void noUsageAfterPurchase_ReturnsFalse() {
    boolean result = pointHistoryRepository.hasUsedPointsAfter(
        user.getId(), purchaseTime, PointProduct.refundReasons());

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("구매 이후의 환불 차감 이력은 사용으로 판정하지 않는다.")
  void refundAfterPurchase_ReturnsFalse() {
    pointHistoryRepository.save(
        new PointHistory(user, -550, PointReason.POINT_REFUND_550, purchaseTime.plusDays(1)));

    boolean result = pointHistoryRepository.hasUsedPointsAfter(
        user.getId(), purchaseTime, PointProduct.refundReasons());

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("구매 이전의 사용 이력은 판정에 포함되지 않는다.")
  void usageBeforePurchase_ReturnsFalse() {
    pointHistoryRepository.save(
        new PointHistory(user, -400, PointReason.THEME_PURCHASE, purchaseTime.minusDays(1)));

    boolean result = pointHistoryRepository.hasUsedPointsAfter(
        user.getId(), purchaseTime, PointProduct.refundReasons());

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("구매 이후의 적립(양수) 이력은 사용으로 판정하지 않는다.")
  void earnAfterPurchase_ReturnsFalse() {
    pointHistoryRepository.save(
        new PointHistory(user, 10, PointReason.GUESTBOOK_REWARD, purchaseTime.plusDays(1)));

    boolean result = pointHistoryRepository.hasUsedPointsAfter(
        user.getId(), purchaseTime, PointProduct.refundReasons());

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("다른 사용자의 사용 이력은 판정에 포함되지 않는다.")
  void otherUsersUsage_ReturnsFalse() {
    User other = userRepository.save(createUser("other@gmail.com", "other"));
    pointHistoryRepository.save(
        new PointHistory(other, -400, PointReason.THEME_PURCHASE, purchaseTime.plusDays(1)));

    boolean result = pointHistoryRepository.hasUsedPointsAfter(
        user.getId(), purchaseTime, PointProduct.refundReasons());

    assertThat(result).isFalse();
  }

  private User createUser(String email, String name) {
    return User.builder()
        .email(email)
        .name(name)
        .nickname("nickname")
        .profileImage("profile")
        .provider(Provider.GOOGLE)
        .providerId("provId-" + email)
        .status(Status.ONLINE)
        .lastLogin(LocalDateTime.of(2025, 1, 1, 1, 1))
        .refreshToken("refToken")
        .build();
  }
}
