package com.roome.domain.guestbook.notificationEvent;

import com.roome.domain.notification.entity.NotificationType;
import com.roome.global.notificationEvent.NotificationEvent;

public class GuestBookCreatedEvent extends NotificationEvent {
    private final Long targetId;

    public GuestBookCreatedEvent(Object source, Long senderId, Long receiverId, Long targetId) {
        super(source, senderId, receiverId);
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
