package com.roome.domain.event.service;

import com.roome.domain.event.entity.EventStatus;
import com.roome.domain.event.entity.FirstComeEvent;
import com.roome.domain.event.repository.FirstComeEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstComeEventScheduler {

  private final FirstComeEventRepository firstComeEventRepository;

  // 테스트 환경: 5분마다 이벤트 자동 생성
  @Scheduled(cron = "0 */5 * * * *")
  public void createTestEvent() {
    log.info("🔹 [테스트] 주간 선착순 이벤트 자동 생성 시작");

    FirstComeEvent event = FirstComeEvent.builder()
        .eventName("테스트 선착순 이벤트")
        .rewardPoints(200)
        .maxParticipants(3)
        .eventTime(LocalDateTime.now()) // 즉시 진행
        .status(EventStatus.ONGOING) // 바로 진행 중 상태
        .build();

    firstComeEventRepository.save(event);
    log.info("✅ [테스트] 이벤트 생성 완료: {}", event.getEventTime());
  }

  // 테스트 환경: 5분 이상 지난 이벤트 자동 종료
  @Scheduled(cron = "0 */3 * * * *") // 1분마다 실행
  public void updateEndedTestEvents() {
    List<FirstComeEvent> ongoingEvents = firstComeEventRepository.findByStatus(EventStatus.ONGOING);
    LocalDateTime now = LocalDateTime.now().minusMinutes(10); // 5분 이상 지난 이벤트 종료

    for (FirstComeEvent event : ongoingEvents) {
      if (event.getEventTime().isBefore(now)) {
        event.endEvent();
        firstComeEventRepository.save(event);
        log.info("🚫 [테스트] 이벤트 종료됨: {}", event.getEventName());
      }
    }
  }
}
