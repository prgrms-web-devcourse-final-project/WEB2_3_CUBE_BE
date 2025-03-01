package com.roome.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CreateNotificationRequest {
    private Long senderId;
    private Long targetId;
    private Long receiverId;
    private LocalDateTime createdAt;
    private NotificationType type;
}
