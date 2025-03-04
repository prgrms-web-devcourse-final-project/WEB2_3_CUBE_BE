//package com.roome.domain.point.service;
//
//import com.roome.domain.point.dto.PointBalanceResponse;
//import com.roome.domain.point.dto.PointHistoryResponse;
//import com.roome.domain.point.entity.Point;
//import com.roome.domain.point.entity.PointHistory;
//import com.roome.domain.point.entity.PointReason;
//import com.roome.domain.point.exception.DuplicatePointEarnException;
//import com.roome.domain.point.exception.InsufficientPointsException;
//import com.roome.domain.point.repository.PointHistoryRepository;
//import com.roome.domain.point.repository.PointRepository;
//import com.roome.domain.user.entity.User;
//import com.roome.domain.user.repository.UserRepository;
//import com.roome.global.jwt.exception.UserNotFoundException;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.SliceImpl;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PointServiceTest {
//
//  @Mock
//  private PointRepository pointRepository;
//
//  @Mock
//  private PointHistoryRepository pointHistoryRepository;
//
//  @Mock
//  private UserRepository userRepository;
//
//  @InjectMocks
//  private PointService pointService;
//
//  private User user;
//  private Point point;
//
//  @BeforeEach
//  void setUp() {
//    user = User.builder()
//        .id(1L)
//        .nickname("TestUser")
//        .build();
//
//    point = Point.builder()
//        .user(user)
//        .balance(5000) // 초기 포인트 설정
//        .totalEarned(5000)
//        .totalUsed(0)
//        .build();
//  }
//
//  @Test
//  @DisplayName("포인트 적립 성공")
//  void earnPoints_Success() {
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));
//    when(pointHistoryRepository.existsByUserIdAndReasonAndCreatedAt(1L, PointReason.GUESTBOOK_REWARD, LocalDate.now()))
//        .thenReturn(false);
//
//    pointService.earnPoints(1L, PointReason.GUESTBOOK_REWARD);
//
//    verify(pointRepository, atLeastOnce()).save(point);
//    verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
//  }
//
//  @Test
//  @DisplayName("포인트 적립 실패 - 중복 적립 방지")
//  void earnPoints_Fail_Duplicate() {
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(pointHistoryRepository.existsByUserIdAndReasonAndCreatedAt(1L, PointReason.GUESTBOOK_REWARD, LocalDate.now()))
//        .thenReturn(true);
//
//    assertThatThrownBy(() -> pointService.earnPoints(1L, PointReason.GUESTBOOK_REWARD))
//        .isInstanceOf(DuplicatePointEarnException.class);
//  }
//
//  @Test
//  @DisplayName("포인트 사용 성공")
//  void usePoints_Success() {
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));
//
//    pointService.usePoints(1L, PointReason.THEME_PURCHASE);
//
//    verify(pointRepository, atLeastOnce()).save(point);
//    verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
//  }
//
//  @Test
//  @DisplayName("포인트 사용 실패 - 잔액 부족")
//  void usePoints_Fail_InsufficientPoints() {
//    point = Point.builder()
//        .user(user)
//        .balance(100) // 잔액이 적음
//        .totalEarned(100)
//        .totalUsed(0)
//        .build();
//
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));
//
//    assertThatThrownBy(() -> pointService.usePoints(1L, PointReason.BOOK_UNLOCK_LV3))
//        .isInstanceOf(InsufficientPointsException.class);
//  }
//
//  @Test
//  @DisplayName("포인트 잔액 조회")
//  void getMyPointBalance() {
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));
//
//    PointBalanceResponse response = pointService.getMyPointBalance(1L);
//
//    verify(pointRepository, times(1)).findByUser(user);
//    assert response.getBalance() == 5000;
//  }
//
//  @Test
//  @DisplayName("포인트 내역 조회 - 첫 페이지 (최신순 정렬, 커서 X)")
//  void getPointHistory_FirstPage() {
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));
//    when(pointHistoryRepository.countByUserId(1L)).thenReturn(10L);
//    when(pointHistoryRepository.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class)))
//        .thenReturn(new SliceImpl<>(List.of(
//            new PointHistory(user, 100, PointReason.FIRST_COME_EVENT, LocalDateTime.now()),
//            new PointHistory(user, -400, PointReason.THEME_PURCHASE, LocalDateTime.now().minusMinutes(5))
//        )));
//
//    PointHistoryResponse response = pointService.getPointHistory(1L, 0L, 2);
//
//    assertEquals(5000, response.getBalance());
//    assertEquals(10, response.getTotalCount());
//    assertEquals(2, response.getHistory().size());
//    assertNotNull(response.getFirstId());
//    assertNotNull(response.getLastId());
//    assertNull(response.getNextCursor());
//  }
//
//  @Test
//  @DisplayName("포인트 내역 조회 - 다음 페이지 (커서 기반 페이징)")
//  void getPointHistory_WithCursor() {
//    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//    when(pointRepository.findByUser(user)).thenReturn(Optional.of(point));
//    when(pointHistoryRepository.countByUserId(1L)).thenReturn(10L);
//    when(pointHistoryRepository.findByUserAndIdLessThanOrderByCreatedAtDesc(eq(user), eq(5L), any(Pageable.class)))
//        .thenReturn(new SliceImpl<>(List.of(
//            new PointHistory(user, -500, PointReason.BOOK_UNLOCK_LV2, LocalDateTime.now().minusHours(1)), // ✅ `user` 값 추가
//            new PointHistory(user, 50, PointReason.DAILY_ATTENDANCE, LocalDateTime.now().minusDays(1)) // ✅ `reason` 값 추가
//        ), PageRequest.of(0, 2), true)); // hasNext = true
//
//    PointHistoryResponse response = pointService.getPointHistory(1L, 5L, 2);
//
//    assertEquals(5000, response.getBalance());
//    assertEquals(10, response.getTotalCount());
//    assertEquals(2, response.getHistory().size());
//    assertNotNull(response.getFirstId());
//    assertNotNull(response.getLastId());
//    assertNotNull(response.getNextCursor());
//  }
//
//}
