package com.roome.domain.point.event;

import com.roome.domain.notification.entity.NotificationType;
import com.roome.domain.point.entity.PointReason;
import com.roome.global.notificationEvent.NotificationEvent;
import lombok.Getter;

@Getter
public class PointEvent extends NotificationEvent {
    private final Long targetId; // Point ID
    private final int amount;
    private final PointReason reason;

    public PointEvent(Object source, Long senderId, Long receiverId, Long targetId, int amount, PointReason reason) {
        super(source, senderId, receiverId);
        this.targetId = targetId;
        this.amount = amount;
        this.reason = reason;
    }

    @Override
    public Long getTargetId() {
        return targetId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.POINT;
    }
}
