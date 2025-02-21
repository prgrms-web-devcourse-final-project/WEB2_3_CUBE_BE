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
    private String title;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private Long targetId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
