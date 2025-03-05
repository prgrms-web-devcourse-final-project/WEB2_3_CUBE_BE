package com.roome.domain.event.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "first_come_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirstComeEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String eventName; // 이벤트 이름

  @Column(nullable = false)
  private int rewardPoints; // 보상 포인트

  @Column(nullable = false)
  private int maxParticipants; // 최대 참여 인원

  @Column(nullable = false)
  private LocalDateTime eventTime; // 이벤트 시작 시간

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EventParticipation> participants = new ArrayList<>();

  @Builder
  public FirstComeEvent(String eventName, int rewardPoints, int maxParticipants,
      LocalDateTime eventTime) {
    this.eventName = eventName;
    this.rewardPoints = rewardPoints;
    this.maxParticipants = maxParticipants;
    this.eventTime = eventTime;
  }

  public boolean isEventOpen() {
    return LocalDateTime.now().isAfter(eventTime);
  }
}

