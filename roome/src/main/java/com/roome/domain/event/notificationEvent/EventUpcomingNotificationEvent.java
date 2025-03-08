package com.roome.domain.event.notificationEvent;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.global.notificationEvent.NotificationEvent;

public class EventUpcomingNotificationEvent extends NotificationEvent {
    private final Long eventId;

    public EventUpcomingNotificationEvent(Object source, Long senderId, Long receiverId, Long eventId) {
        super(source, senderId, receiverId);
        this.eventId = eventId;
    }

    @Override
    public Long getTargetId() {
        // 이벤트 ID 반환, 프론트에서 알림 클릭 시 해당 이벤트 페이지로 이동할 때 사용
        return eventId;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EVENT;
    }
}
