package com.roome.domain.notification.service;

import com.roome.domain.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /// 모든 사용자에게 알림을 보냅니다.
    public void sendGlobalNotification() {
        NotificationMessage message = new NotificationMessage();
        messagingTemplate.convertAndSend("/notification/global", message);
    }

    /// 특정 사용자에게 알림을 보냅니다.
    /// @param userId 알림을 받을 사용자 ID
    public void sendPrivateNotification(String userId) {
        NotificationMessage message = new NotificationMessage();
        messagingTemplate.convertAndSendToUser(userId, "/notification/private", message);
    }

    /// DB 이벤트 발생 시 호출되는 메서드 (기존 이벤트 리스너와 연동)
    public void handleDbEvent(String userId) {
        // 특정 사용자에게만 알림 전송
        if (userId != null && !userId.isEmpty()) {
            sendPrivateNotification(userId);
        } else {
            // 특정 사용자가 지정되지 않은 경우 모든 사용자에게 알림
            sendGlobalNotification();
        }
    }
}
