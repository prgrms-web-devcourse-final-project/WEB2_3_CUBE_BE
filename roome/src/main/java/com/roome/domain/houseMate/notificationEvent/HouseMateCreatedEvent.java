package com.roome.domain.houseMate.notificationEvent;

import com.roome.domain.notification.entity.NotificationType;
import com.roome.global.notificationEvent.NotificationEvent;

public class HouseMateCreatedEvent extends NotificationEvent {
    private final Long targetId;

    public HouseMateCreatedEvent(Object source, Long senderId, Long receiverId, Long targetId) {
        super(source, senderId, receiverId);
        this.targetId = targetId;
    }

    @Override
    public Long getTargetId() {
        return targetId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.HOUSE_MATE;
    }
}
