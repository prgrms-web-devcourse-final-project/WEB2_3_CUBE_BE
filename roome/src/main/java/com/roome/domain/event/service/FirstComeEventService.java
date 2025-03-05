package com.roome.domain.event.service;

import com.roome.domain.event.entity.EventParticipation;
import com.roome.domain.event.entity.EventStatus;
import com.roome.domain.event.entity.FirstComeEvent;
import com.roome.domain.event.exception.*;
import com.roome.domain.event.repository.EventParticipationRepository;
import com.roome.domain.event.repository.FirstComeEventRepository;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.service.PointService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.UserNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FirstComeEventService {

  private final FirstComeEventRepository firstComeEventRepository;
  private final EventParticipationRepository eventParticipationRepository;
  private final UserRepository userRepository;
  private final PointService pointService;

  @Transactional
  public void joinEvent(Long userId, Long eventId) {
    FirstComeEvent event = firstComeEventRepository.findById(eventId)
        .orElseThrow(EventNotFoundException::new);

    if (!event.isEventOpen()) {
      throw new EventNotStartedException();
    }

    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    if (eventParticipationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
      throw new AlreadyParticipatedException();
    }

    long participantCount = eventParticipationRepository.countByEventId(eventId);
    if (participantCount >= event.getMaxParticipants()) {
      throw new EventFullException();
    }

    eventParticipationRepository.save(new EventParticipation(user, event, LocalDateTime.now()));

    pointService.earnPoints(user, PointReason.FIRST_COME_EVENT);
  }

  @Transactional(readOnly = true)
  public FirstComeEvent getOngoingEvent() {
    return firstComeEventRepository.findTopByStatusOrderByEventTimeDesc(EventStatus.ONGOING)
        .orElseThrow(EventNotFoundException::new);
  }
}
