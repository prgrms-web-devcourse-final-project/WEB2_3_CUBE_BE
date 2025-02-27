package com.roome.domain.point.service;

import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

  @Mock
  private PointRepository pointRepository;

  @Mock
  private PointHistoryRepository pointHistoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private PointService pointService;

  private User user;
  private Point point;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(1L)
        .nickname("TestUser")
        .lastGuestbookReward(LocalDateTime.now().minusDays(2))
        .build();

    point = Point.builder()
        .user(user)
        .balance(100)
        .totalEarned(100)
        .totalUsed(0)
        .build();
  }

  @Test
  @DisplayName("방명록 작성 보상 적립 - 성공 (한 번도 보상을 받은 적 없음)")
  void addGuestbookReward_FirstTime_Ever() {
    // given
    user.updateLastGuestbookReward(null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

    // when
    pointService.addGuestbookReward(1L);

    // then
    verify(pointRepository, atLeastOnce()).save(point);
    verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
  }


  @Test
  @DisplayName("방명록 작성 보상 적립 - 성공 (이미 포인트 데이터 존재)")
  void addGuestbookReward_ExistingPoint() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

    pointService.addGuestbookReward(1L);

    verify(pointRepository, atLeastOnce()).save(point); // ✅ 최소 1번 호출 검증
    verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
  }

  @Test
  @DisplayName("방명록 작성 보상 적립 - 실패 (이미 오늘 보상 받음)")
  void addGuestbookReward_Fail_AlreadyReceivedToday() {
    user.updateLastGuestbookReward(LocalDateTime.now());

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

    pointService.addGuestbookReward(1L);

    verify(pointRepository, never()).save(any(Point.class));
    verify(pointHistoryRepository, never()).save(any(PointHistory.class));
  }

  @Test
  @DisplayName("방명록 작성 보상 적립 - 실패 (사용자 없음)")
  void addGuestbookReward_Fail_UserNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pointService.addGuestbookReward(1L))
        .isInstanceOf(UserNotFoundException.class);

    verify(pointRepository, never()).save(any(Point.class));
    verify(pointHistoryRepository, never()).save(any(PointHistory.class));
  }
}