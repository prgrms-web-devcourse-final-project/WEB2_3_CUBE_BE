package com.roome.global.notificationEvent;

import com.roome.domain.notification.dto.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class NotificationEvent extends ApplicationEvent {
    private final Long senderId;
    private final Long receiverId;

    public NotificationEvent(Object source, Long senderId, Long receiverId) {
        super(source);
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public abstract Long getTargetId();
    public abstract NotificationType getType();
}