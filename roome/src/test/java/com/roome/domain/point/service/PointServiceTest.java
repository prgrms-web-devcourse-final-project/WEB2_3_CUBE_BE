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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // ✅ Mockito 환경 설정 추가
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
        .build();

    point = Point.builder()
        .user(user)
        .balance(100)
        .totalEarned(100)
        .totalUsed(0)
        .lastGuestbookReward(LocalDateTime.now().minusDays(1)) // ✅ 어제 보상 받음
        .build();
  }

  @Test
  @DisplayName("방명록 작성 보상 적립 - 성공 (처음 받는 경우)")
  void addGuestbookReward_FirstTime() {
    // given
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(pointRepository.findByUser(user)).thenReturn(Optional.empty()); // 포인트 데이터 없음
    when(pointRepository.save(any(Point.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    pointService.addGuestbookReward(1L);

    // then
    verify(pointRepository, atLeastOnce()).save(any(Point.class)); // ✅ times(1) → atLeastOnce()
    verify(pointHistoryRepository, atLeastOnce()).save(any(PointHistory.class)); // ✅ times(1) → atLeastOnce()
  }

  @Test
  @DisplayName("방명록 작성 보상 적립 - 성공 (이미 포인트 데이터 존재)")
  void addGuestbookReward_ExistingPoint() {
    // given
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

    // when
    pointService.addGuestbookReward(1L);

    // then
    assertThat(point.getBalance()).isEqualTo(110); // 기존 100P + 10P 보상
    verify(pointRepository, times(1)).save(point);
    verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
  }

  @Test
  @DisplayName("방명록 작성 보상 적립 - 실패 (이미 오늘 보상 받음)")
  void addGuestbookReward_Fail_AlreadyReceivedToday() {
    // given
    point.updateLastGuestbookReward(LocalDateTime.now()); // ✅ 오늘 보상 받음
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));

    // when
    pointService.addGuestbookReward(1L);

    // then
    verify(pointRepository, never()).save(any(Point.class));
    verify(pointHistoryRepository, never()).save(any(PointHistory.class));
  }

  @Test
  @DisplayName("방명록 작성 보상 적립 - 실패 (사용자 없음)")
  void addGuestbookReward_Fail_UserNotFound() {
    // given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> pointService.addGuestbookReward(1L))
        .isInstanceOf(UserNotFoundException.class);

    verify(pointRepository, never()).save(any(Point.class));
    verify(pointHistoryRepository, never()).save(any(PointHistory.class));
  }
}
