package com.roome.domain.event.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstComeEventScheduler {

  private final FirstComeEventService firstComeEventService;

  // 매주 수요일 18:00 이벤트 자동 생성
  @Scheduled(cron = "15 12 21 ? * TUE")
  public void createWeeklyEvent() {
    log.info("이벤트 자동 생성");
    firstComeEventService.createEvent("주간 선착순 이벤트", 200, 1, LocalDateTime.now().plusSeconds(10));
    log.info("이벤트 생성 완료");
  }
}
