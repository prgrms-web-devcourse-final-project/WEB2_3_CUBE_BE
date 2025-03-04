package com.roome.domain.rank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.entity.UserActivity;
import com.roome.domain.rank.repository.UserActivityRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
public class UserActivityServiceTest {

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private UserActivityRepository userActivityRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private ValueOperations<String, Object> valueOperations;

  @Mock
  private ZSetOperations<String, Object> zSetOperations;

  @InjectMocks
  private UserActivityService userActivityService;

  private User testUser;
  private Room testRoom;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).name("Test User").build();

    testRoom = Room.builder().id(1L).user(testUser).build();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
  }

  @Test
  @DisplayName("출석 체크 - 오전/오후 구분 테스트")
  void testAttendanceActivityMorningAfternoon() {
    // Given
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    // 오전 출석 (키가 없는 경우)
    when(redisTemplate.hasKey(anyString())).thenReturn(false);

    // When
    boolean morningResult = userActivityService.recordUserActivity(userId, ActivityType.ATTENDANCE,
        null);

    // Then
    assertTrue(morningResult);
    verify(userActivityRepository).save(any(UserActivity.class));
    verify(redisTemplate.opsForZSet()).incrementScore(eq("user:ranking"), eq(userId.toString()),
        eq(1.0));

    // Given - 오후 출석 시도
    when(redisTemplate.hasKey(anyString())).thenReturn(false);

    // When
    boolean afternoonResult = userActivityService.recordUserActivity(userId,
        ActivityType.ATTENDANCE, null);

    // Then
    assertTrue(afternoonResult);
    verify(userActivityRepository, times(2)).save(any(UserActivity.class));
    verify(redisTemplate.opsForZSet(), times(2)).incrementScore(eq("user:ranking"),
        eq(userId.toString()), eq(1.0));
  }

  @Test
  @DisplayName("출석 체크 - 같은 시간대 중복 체크 방지")
  void testAttendanceActivityDuplicatePrevention() {
    // Given
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    // 첫 출석 (키가 없는 경우)
    when(redisTemplate.hasKey(anyString())).thenReturn(false).thenReturn(true);

    // When
    boolean firstResult = userActivityService.recordUserActivity(userId, ActivityType.ATTENDANCE,
        null);
    boolean secondResult = userActivityService.recordUserActivity(userId, ActivityType.ATTENDANCE,
        null);

    // Then
    assertTrue(firstResult);
    assertFalse(secondResult);
    verify(userActivityRepository, times(1)).save(any(UserActivity.class));
    verify(redisTemplate.opsForZSet(), times(1)).incrementScore(anyString(), anyString(),
        anyDouble());
  }

  @Test
  @DisplayName("서평 작성 - 길이 제한 테스트")
  void testBookReviewLengthLimit() {
    // Given
    Long userId = 1L;
    Long bookId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    // When - 30자 미만
    boolean shortReviewResult = userActivityService.recordUserActivity(userId,
        ActivityType.BOOK_REVIEW, bookId, 25);

    // Then
    assertFalse(shortReviewResult);
    verify(userActivityRepository, never()).save(any(UserActivity.class));

    // When -30자 이상
    boolean validReviewResult = userActivityService.recordUserActivity(userId,
        ActivityType.BOOK_REVIEW, bookId, 35);

    // Then
    assertTrue(validReviewResult);
    verify(userActivityRepository).save(any(UserActivity.class));
    verify(redisTemplate.opsForZSet()).incrementScore(eq("user:ranking"), eq(userId.toString()),
        eq(15.0));
  }

  @Test
  @DisplayName("방명록 작성 - 길이 제한 테스트")
  void testGuestbookLengthLimit() {
    // Given
    Long userId = 1L;
    Long roomId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    // When - 15자 미만
    boolean shortGuestbookResult = userActivityService.recordUserActivity(userId,
        ActivityType.GUESTBOOK, roomId, 10);

    // Then
    assertFalse(shortGuestbookResult);
    verify(userActivityRepository, never()).save(any(UserActivity.class));

    // When - 15자 이상
    boolean validGuestbookResult = userActivityService.recordUserActivity(userId,
        ActivityType.GUESTBOOK, roomId, 20);

    // Then
    assertTrue(validGuestbookResult);
    verify(userActivityRepository).save(any(UserActivity.class));
    verify(redisTemplate.opsForZSet()).incrementScore(eq("user:ranking"), eq(userId.toString()),
        eq(5.0));
  }

  @Test
  @DisplayName("방 방문 - 방문자/호스트 모두에게 점수 부여")
  void testRoomVisit() {
    // Given
    Long visitorId = 1L;
    Long hostId = 2L;
    User host = User.builder().id(hostId).name("Host User").build();
    Room room = Room.builder().id(1L).user(host).build();

    when(userRepository.findById(visitorId)).thenReturn(Optional.of(testUser));
    when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
    when(roomRepository.findByUserId(hostId)).thenReturn(Optional.of(room));
    when(redisTemplate.hasKey(anyString())).thenReturn(false);

    // When
    boolean result = userActivityService.recordVisit(visitorId, hostId);

    // Then
    assertTrue(result);

    // 방문자에게 ROOM_VISIT 활동 기록 및 +1점
    ArgumentCaptor<UserActivity> visitorActivityCaptor = ArgumentCaptor.forClass(
        UserActivity.class);
    verify(userActivityRepository, times(2)).save(visitorActivityCaptor.capture());
    UserActivity visitorActivity = visitorActivityCaptor.getAllValues().get(0);
    assertEquals(ActivityType.ROOM_VISIT, visitorActivity.getActivityType());
    assertEquals(visitorId, visitorActivity.getUser().getId());
    verify(redisTemplate.opsForZSet()).incrementScore(eq("user:ranking"), eq(visitorId.toString()),
        eq(1.0));

    // 호스트에게 VISITOR_COUNT 활동 기록 및 +2점
    UserActivity hostActivity = visitorActivityCaptor.getAllValues().get(1);
    assertEquals(ActivityType.VISITOR_COUNT, hostActivity.getActivityType());
    assertEquals(hostId, hostActivity.getUser().getId());
    verify(redisTemplate.opsForZSet()).incrementScore(eq("user:ranking"), eq(hostId.toString()),
        eq(2.0));
  }

  @Test
  @DisplayName("방 방문 - 24시간 내 중복 방문 방지")
  void testRoomVisitDuplicatePrevention() {
    // Given
    Long visitorId = 1L;
    Long hostId = 2L;
    User host = User.builder().id(hostId).name("Host User").build();
    Room room = Room.builder().id(1L).user(host).build();

    when(userRepository.findById(visitorId)).thenReturn(Optional.of(testUser));
    when(userRepository.findById(hostId)).thenReturn(Optional.of(host));
    when(roomRepository.findByUserId(hostId)).thenReturn(Optional.of(room));

    // 첫 방문 (키가 없는 경우)
    when(redisTemplate.hasKey(anyString())).thenReturn(false).thenReturn(true);

    // When
    boolean firstVisitResult = userActivityService.recordVisit(visitorId, hostId);
    boolean secondVisitResult = userActivityService.recordVisit(visitorId, hostId);

    // Then
    assertTrue(firstVisitResult);
    assertFalse(secondVisitResult);
    verify(userActivityRepository, times(2)).save(
        any(UserActivity.class)); // 첫 방문에서 visitor와 host 각각 한 번씩
    verify(redisTemplate.opsForZSet(), times(2)).incrementScore(anyString(), anyString(),
        anyDouble());
  }

  @Test
  @DisplayName("팔로워 증가 - 팔로잉 받은 사람에게 점수 부여")
  void testFollowerIncrease() {
    // Given
    Long followerId = 1L;
    Long followingId = 2L;
    User following = User.builder().id(followingId).name("Following User").build();

    when(userRepository.findById(followingId)).thenReturn(Optional.of(following));

    // When
    boolean result = userActivityService.recordFollowActivity(followerId, followingId);

    // Then
    assertTrue(result);

    // 팔로잉 받은 사람에게 FOLLOWER_INCREASE 활동 기록 및 +5점
    ArgumentCaptor<UserActivity> activityCaptor = ArgumentCaptor.forClass(UserActivity.class);
    verify(userActivityRepository).save(activityCaptor.capture());
    UserActivity activity = activityCaptor.getValue();
    assertEquals(ActivityType.FOLLOWER_INCREASE, activity.getActivityType());
    assertEquals(followingId, activity.getUser().getId());
    verify(redisTemplate.opsForZSet()).incrementScore(eq("user:ranking"),
        eq(followingId.toString()), eq(5.0));
  }

  @Test
  @DisplayName("일일 활동 제한 - 서평 작성 한도 테스트")
  void testDailyLimitBookReview() {
    // Given
    Long userId = 1L;
    Long bookId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(redisTemplate.opsForValue().increment(anyString(), eq(0L))).thenReturn(0L).thenReturn(1L)
        .thenReturn(2L).thenReturn(3L);

    // When & Then - 3번까지는 성공
    for (int i = 0; i < 3; i++) {
      boolean result = userActivityService.recordUserActivity(userId, ActivityType.BOOK_REVIEW,
          bookId, 35);
      assertTrue(result);
    }

    // 4번째는 실패
    boolean fourthResult = userActivityService.recordUserActivity(userId, ActivityType.BOOK_REVIEW,
        bookId, 35);
    assertFalse(fourthResult);

    verify(userActivityRepository, times(3)).save(any(UserActivity.class));
    verify(redisTemplate.opsForZSet(), times(3)).incrementScore(anyString(), anyString(),
        anyDouble());
  }
}