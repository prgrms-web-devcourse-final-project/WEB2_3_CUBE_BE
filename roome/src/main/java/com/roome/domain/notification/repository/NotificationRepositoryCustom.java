package com.roome.domain.notification.repository;

import com.roome.domain.notification.dto.NotificationSearchCondition;
import com.roome.domain.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepositoryCustom {
    List<Notification> findNotifications(NotificationSearchCondition condition);
    void deleteOldNotifications(LocalDateTime threshold);
}
