package com.roome.domain.event.service;

import com.roome.domain.event.entity.EventParticipation;
import com.roome.domain.event.entity.FirstComeEvent;
import com.roome.domain.event.exception.AlreadyParticipatedException;
import com.roome.domain.event.exception.EventFullException;
import com.roome.domain.event.exception.EventNotFoundException;
import com.roome.domain.event.exception.EventNotStartedException;
import com.roome.domain.event.repository.EventParticipationRepository;
import com.roome.domain.event.repository.FirstComeEventRepository;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirstComeEventService {

  private final FirstComeEventRepository firstComeEventRepository;
  private final EventParticipationRepository eventParticipationRepository;
  private final UserRepository userRepository;
  private final PointService pointService;

  // 자동 이벤트 생성 (테스트 중에는 일정 조정 가능)
  @Transactional
  public void createEvent(String eventName, int rewardPoints, int maxParticipants, LocalDateTime eventTime) {
    FirstComeEvent event = FirstComeEvent.builder()
        .eventName(eventName)
        .rewardPoints(rewardPoints)
        .maxParticipants(maxParticipants)
        .eventTime(eventTime)
        .build();
    firstComeEventRepository.save(event);
  }

  // 선착순 이벤트 참여
  @Transactional
  public void joinEvent(Long userId, Long eventId) {
    FirstComeEvent event = firstComeEventRepository.findById(eventId)
        .orElseThrow(EventNotFoundException::new);

    // 이벤트가 시작되었는지 확인
    if (!event.isEventOpen()) {
      throw new EventNotStartedException();
    }

    // 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    // 이미 참여했는지 확인
    if (eventParticipationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
      throw new AlreadyParticipatedException();
    }

    // 현재 참여 인원 체크
    long participantCount = eventParticipationRepository.countByEventId(eventId);
    if (participantCount >= event.getMaxParticipants()) {
      throw new EventFullException();
    }

    // 이벤트 참여 기록 저장
    eventParticipationRepository.save(new EventParticipation(user, event, LocalDateTime.now()));

    // 포인트 지급
    pointService.earnPoints(user, PointReason.FIRST_COME_EVENT);
  }
}
