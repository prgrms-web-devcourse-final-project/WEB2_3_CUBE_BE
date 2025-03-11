package com.roome.domain.notification.service;

import com.roome.domain.notification.entity.NotificationType;
import com.roome.domain.notification.dto.NotificationWebSocketMessageDto;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /// 특정 사용자에게 알림을 전송합니다.
    /// @param receiverId 수신자 ID
    /// @param notificationId 알림 ID
    /// @param type 알림 유형
    /// @throws BusinessException 웹소켓 메시지 전송 과정에서 발생한 오류에 대한 비즈니스 예외
    public void sendNotificationToUser(Long receiverId, Long notificationId, NotificationType type) {
        // 파라미터 유효성 검사
        validateParameters(receiverId, notificationId, type);

        NotificationWebSocketMessageDto messageDto = NotificationWebSocketMessageDto.of(notificationId, type, receiverId);

        log.info("알림 메시지 전송 시도: 수신자={}, 알림ID={}, 유형={}, 시간={}",
                receiverId, notificationId, type, messageDto.getTimestamp());

        try {
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),  // 사용자 ID
                    "/notification",        // 목적지
                    messageDto              // 메시지
            );
            log.info("알림 메시지 전송 성공: 수신자={}, 알림ID={}", receiverId, notificationId);
        } catch (MessageDeliveryException e) {
            log.error("웹소켓 메시지 전송 실패: 수신자={}, 알림ID={}, 오류={}",
                    receiverId, notificationId, e.getMessage());
            // 사용자가 연결되지 않았거나 대상을 찾을 수 없는 경우
            throw new BusinessException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
        } catch (IllegalStateException e) {
            log.error("웹소켓 브로커 상태 오류: 수신자={}, 알림ID={}, 오류={}",
                    receiverId, notificationId, e.getMessage());
            // 메시지 브로커가 초기화되지 않았거나 종료된 경우
            throw new BusinessException(ErrorCode.NOTIFICATION_BROKER_ERROR);
        } catch (Exception e) {
            log.error("알림 전송 중 예기치 않은 오류: 수신자={}, 알림ID={}, 오류 타입={}, 오류={}",
                    receiverId, notificationId, e.getClass().getName(), e.getMessage());
            throw new BusinessException(ErrorCode.NOTIFICATION_SENDING_ERROR);
        }
    }

    /// 알림 전송에 필요한 파라미터의 유효성을 검사합니다.
    /// @param receiverId 수신자 ID
    /// @param notificationId 알림 ID
    /// @param type 알림 유형
    /// @throws BusinessException 파라미터가 유효하지 않을 경우 발생하는 비즈니스 예외
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