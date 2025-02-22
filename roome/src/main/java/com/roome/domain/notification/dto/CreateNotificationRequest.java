package com.roome.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationRequest {
    private Long senderId;
    private Long targetId;
    private Long receiverId;
    private NotificationType type;
}
