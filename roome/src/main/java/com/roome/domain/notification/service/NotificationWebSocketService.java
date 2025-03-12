package com.roome.domain.notification.service;

import com.roome.domain.notification.entity.NotificationType;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    // 새로 추가된 서비스
    private final NotificationRedisService notificationRedisService;

    // 기존 메소드는 유지하되 내부 구현을 Redis 사용 방식으로 변경
    public void sendNotificationToUser(Long receiverId, Long notificationId, NotificationType type) {
        // 파라미터 유효성 검사
        validateParameters(receiverId, notificationId, type);

        log.info("알림 메시지 전송 시도: 수신자={}, 알림ID={}, 유형={}",
                receiverId, notificationId, type);

        try {
            // Redis Pub/Sub을 통해 메시지 발행
            notificationRedisService.publishNotification(receiverId, notificationId, type);
            log.info("알림 메시지 전송 요청 성공: 수신자={}, 알림ID={}", receiverId, notificationId);
        } catch (Exception e) {
            log.error("알림 전송 중 예기치 않은 오류: 수신자={}, 알림ID={}, 오류 타입={}, 오류={}",
                    receiverId, notificationId, e.getClass().getName(), e.getMessage());
            throw new BusinessException(ErrorCode.NOTIFICATION_SENDING_ERROR);
        }
    }

    // 기존 유효성 검사 메소드 그대로 유지
    private void validateParameters(Long receiverId, Long notificationId, NotificationType type) {
        if (receiverId == null) {
            log.error("알림 전송 실패: 수신자 ID가 null입니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_INVALID_RECEIVER);
        }

        if (notificationId == null) {
            log.error("알림 전송 실패: 알림 ID가 null입니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_INVALID_ID);
        }

        if (type == null) {
            log.error("알림 전송 실패: 알림 타입이 null입니다.");
            throw new BusinessException(ErrorCode.NOTIFICATION_INVALID_TYPE);
        }
    }
}