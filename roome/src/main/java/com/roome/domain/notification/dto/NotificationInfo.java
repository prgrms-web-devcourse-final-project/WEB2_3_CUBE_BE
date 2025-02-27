package com.roome.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
public class NotificationInfo {
    private Long notificationId;
    private NotificationType type;
    private Long senderId;
    private String senderNickName;
    private String senderProfileImage;
    private Long targetId;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
