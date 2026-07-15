package com.roome.domain.rank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.service.PointService;
import com.roome.domain.rank.repository.UserActivityRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
public class RankingSchedulerTest {

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ZSetOperations<String, Object> zSetOperations;

  @Mock
  private UserActivityRepository userActivityRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PointService pointService;

  @InjectMocks
  private RankingScheduler rankingScheduler;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
  }

  @DisplayName("주간 랭킹 보상 지급 테스트")
  @Test
  void awardWeeklyPointsTest() {
    // Given
    Set<ZSetOperations.TypedTuple<Object>> topRankers = new LinkedHashSet<>();

    // 1등 모킹
    ZSetOperations.TypedTuple<Object> firstRanker = Mockito.mock(ZSetOperations.TypedTuple.class);
    when(firstRanker.getValue()).thenReturn("1");
    when(firstRanker.getScore()).thenReturn(100.0);
    topRankers.add(firstRanker);

    // 2등 모킹
    ZSetOperations.TypedTuple<Object> secondRanker = Mockito.mock(ZSetOperations.TypedTuple.class);
    when(secondRanker.getValue()).thenReturn("2");
    when(secondRanker.getScore()).thenReturn(80.0);
    topRankers.add(secondRanker);

    // 3등 모킹
    ZSetOperations.TypedTuple<Object> thirdRanker = Mockito.mock(ZSetOperations.TypedTuple.class);
    when(thirdRanker.getValue()).thenReturn("3");
    when(thirdRanker.getScore()).thenReturn(70.0);
    topRankers.add(thirdRanker);

    when(zSetOperations.reverseRangeWithScores("user:ranking", 0, 2)).thenReturn(topRankers);

    // 유저 정보 모킹
    User user1 = Mockito.mock(User.class);
    User user2 = Mockito.mock(User.class);
    User user3 = Mockito.mock(User.class);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
    when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
    when(userRepository.findById(3L)).thenReturn(Optional.of(user3));

    // When
    rankingScheduler.awardWeeklyPoints();

    // Then: 순위별 사유로 PointService 단일 경로를 통해 지급된다 (원자 적립 + 이력 + 캐시 무효화)
    verify(pointService).earnPoints(user1, PointReason.RANK_1);
    verify(pointService).earnPoints(user2, PointReason.RANK_2);
    verify(pointService).earnPoints(user3, PointReason.RANK_3);

    verify(userActivityRepository).deleteAllByCreatedAtBefore(any(LocalDateTime.class));

    // redisTemplate.delete는 총 2번 호출됨 (awardWeeklyPoints와 updateRanking에서 각각)
    verify(redisTemplate, times(2)).delete("user:ranking");
  }
}
