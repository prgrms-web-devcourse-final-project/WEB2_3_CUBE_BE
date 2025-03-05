package com.roome.domain.event.dto;

import com.roome.domain.event.entity.FirstComeEvent;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FirstComeEventResponse {

  private final Long id;
  private final String eventName;
  private final int rewardPoints;
  private final int maxParticipants;
  private final LocalDateTime eventTime;

  public static FirstComeEventResponse fromEntity(FirstComeEvent event) {
    return new FirstComeEventResponse(
        event.getId(),
        event.getEventName(),
        event.getRewardPoints(),
        event.getMaxParticipants(),
        event.getEventTime()
    );
  }
}
