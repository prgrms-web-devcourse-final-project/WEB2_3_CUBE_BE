package com.roome.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationWebSocketMessageDto {
    private Long notificationId;
    private NotificationType type;
    private Long receiverId;
    private LocalDateTime timestamp;

    public static NotificationWebSocketMessageDto of(Long notificationId, NotificationType type, Long receiverId) {
        return NotificationWebSocketMessageDto.builder()
                .notificationId(notificationId)
                .type(type)
                .receiverId(receiverId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}