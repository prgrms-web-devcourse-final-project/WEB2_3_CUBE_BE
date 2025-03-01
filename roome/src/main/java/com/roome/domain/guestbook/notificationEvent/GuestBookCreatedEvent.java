package com.roome.domain.guestbook.notificationEvent;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.global.notificationEvent.NotificationEvent;

import java.time.LocalDateTime;

public class GuestBookCreatedEvent extends NotificationEvent {
    private final Long targetId;

    public GuestBookCreatedEvent(Object source, Long senderId, Long receiverId, Long targetId, LocalDateTime createAt) {
        super(source, senderId, receiverId, createAt);
        this.targetId = targetId;
    }

    @Override
    public Long getTargetId() {
        return targetId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.GUESTBOOK;
    }
}
