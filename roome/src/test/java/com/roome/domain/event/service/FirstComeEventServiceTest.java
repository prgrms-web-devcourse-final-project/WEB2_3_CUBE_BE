package com.roome.domain.event.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.event.entity.EventStatus;
import com.roome.domain.event.entity.FirstComeEvent;
import com.roome.domain.event.repository.EventParticipationRepository;
import com.roome.domain.event.repository.FirstComeEventRepository;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirstComeEventServiceTest {

  @InjectMocks
  private FirstComeEventService firstComeEventService;

  @Mock
  private FirstComeEventRepository firstComeEventRepository;

  @Mock
  private EventParticipationRepository eventParticipationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PointService pointService;

  @Test
  @DisplayName("선착순 이벤트 참여 시 사유 기본값이 아니라 이벤트별 rewardPoints로 적립되어야 한다.")
  void joinEvent_UsesEventRewardPoints() {
    // given: 기본값(200)과 다른 500포인트를 지급하는 이벤트
    FirstComeEvent event = FirstComeEvent.builder()
        .eventName("특별 이벤트")
        .rewardPoints(500)
        .maxParticipants(3)
        .eventTime(LocalDateTime.now().minusMinutes(1)) // 이미 시작됨
        .status(EventStatus.ONGOING)
        .build();
    User user = User.builder().id(1L).nickname("user").build();

    when(firstComeEventRepository.findById(10L)).thenReturn(Optional.of(event));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(eventParticipationRepository.existsByUserIdAndEventId(1L, 10L)).thenReturn(false);
    when(eventParticipationRepository.countByEventId(10L)).thenReturn(0L);

    // when
    firstComeEventService.joinEvent(1L, 10L);

    // then: 카탈로그 기본값(200)이 아닌 이벤트 설정값(500)으로 적립
    verify(eventParticipationRepository).save(any());
    verify(pointService).earnPoints(user, PointReason.FIRST_COME_EVENT, 500);
  }
}
