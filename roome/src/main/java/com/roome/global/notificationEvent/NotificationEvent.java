package com.roome.global.notificationEvent;

import com.roome.domain.notification.dto.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public abstract class NotificationEvent extends ApplicationEvent {
    private final Long senderId;
    private final Long receiverId;
    private final LocalDateTime createdAt;

    public NotificationEvent(Object source, Long senderId, Long receiverId, LocalDateTime createdAt) {
        super(source);
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.createdAt = createdAt;
    }

    public abstract Long getTargetId();
    public abstract NotificationType getType();
}