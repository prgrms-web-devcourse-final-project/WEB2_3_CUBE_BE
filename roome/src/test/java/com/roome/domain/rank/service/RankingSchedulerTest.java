package com.roome.domain.rank.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
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
  private PointRepository pointRepository;

  @Mock
  private PointHistoryRepository pointHistoryRepository;

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

    // 유저 정보 모킹 - ID 명시적 설정
    User user1 = Mockito.mock(User.class);
    when(user1.getId()).thenReturn(1L);

    User user2 = Mockito.mock(User.class);
    when(user2.getId()).thenReturn(2L);

    User user3 = Mockito.mock(User.class);
    when(user3.getId()).thenReturn(3L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
    when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
    when(userRepository.findById(3L)).thenReturn(Optional.of(user3));

    // 포인트 정보 모킹
    Point point1 = Mockito.mock(Point.class);
    Point point2 = Mockito.mock(Point.class);
    Point point3 = Mockito.mock(Point.class);

    when(pointRepository.findByUserId(1L)).thenReturn(Optional.of(point1));
    when(pointRepository.findByUserId(2L)).thenReturn(Optional.of(point2));
    when(pointRepository.findByUserId(3L)).thenReturn(Optional.of(point3));

    // When
    rankingScheduler.awardWeeklyPoints();

    // Then
    verify(point1).addPoints(100);
    verify(point2).addPoints(70);
    verify(point3).addPoints(50);

    verify(pointRepository, times(3)).save(any(Point.class));
    verify(pointHistoryRepository, times(3)).save(any(PointHistory.class));

    verify(userActivityRepository).deleteAllByCreatedAtBefore(any(LocalDateTime.class));

    // redisTemplate.delete는 총 2번 호출됨 (awardWeeklyPoints와 updateRanking에서 각각)
    verify(redisTemplate, times(2)).delete("user:ranking");
  }
}
