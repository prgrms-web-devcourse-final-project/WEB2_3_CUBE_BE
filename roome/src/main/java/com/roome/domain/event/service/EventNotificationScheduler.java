package com.roome.domain.event.service;

import com.roome.domain.event.notificationEvent.EventUpcomingNotificationEvent;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventNotificationScheduler {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 시스템 사용자 ID (알림 발신자로 사용)
    private static final Long SYSTEM_USER_ID = 0L;
    // 모든 사용자에게 이벤트 알림 발송
    @Scheduled(cron = "0 0 11 ? * *")
    public  void sendEventNotifications() {
        log.info("이벤트 알림 발송 시작: 이벤트 ID={}, 이벤트명={}", "0", "선찬순 이벤트");
        int page = 0;
        boolean hasMoreUsers = true;
        int pageSize = 100;
        try {
            // 모든 활성 사용자 조회 (실제 구현 시 적절한 필터링 조건 추가)
            while (hasMoreUsers) {
                PageRequest pageRequest = PageRequest.of(page, pageSize);
                Page<User> userPage = userRepository.findAll(pageRequest);

                if (userPage.isEmpty()) {
                    hasMoreUsers = false;
                    continue;
                }

                // 현재 페이지의 사용자들에게 알림 전송
                for (User user : userPage.getContent()) {
                    try {
                        // 각 사용자에게 이벤트 알림 발행
                        eventPublisher.publishEvent(new EventUpcomingNotificationEvent(
                                this,
                                SYSTEM_USER_ID,  // 발신자 (시스템)
                                user.getId(),    // 수신자 (사용자)
                                0L    // 이벤트 ID
                        ));
                        log.debug("사용자 알림 발행 성공: 사용자ID={}", user.getId());
                    } catch (Exception e) {
                        log.error("사용자 이벤트 알림 발행 실패: 사용자ID={}, 오류={}", user.getId(), e.getMessage());
                        // 개별 사용자 알림 실패가 전체 프로세스를 중단하지 않도록 예외 처리
                    }
                }
                page++;
                hasMoreUsers = userPage.hasNext();
            }

            log.info("이벤트 알림 발송 완료: 모든 사용자에게 알림 전송");
        } catch (Exception e) {
            log.error("이벤트 알림 발송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
