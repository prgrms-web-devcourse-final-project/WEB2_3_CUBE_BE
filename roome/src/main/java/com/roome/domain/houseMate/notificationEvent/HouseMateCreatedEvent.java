package com.roome.domain.houseMate.notificationEvent;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.global.notificationEvent.NotificationEvent;

import java.time.LocalDateTime;

public class HouseMateCreatedEvent extends NotificationEvent {
    private final Long targetId;

    public HouseMateCreatedEvent(Object source, Long senderId, Long receiverId, Long targetId, LocalDateTime createdAt) {
        super(source, senderId, receiverId, createdAt);
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
