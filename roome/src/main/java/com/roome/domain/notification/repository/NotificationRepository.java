package com.roome.domain.notification.repository;

import com.roome.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {
    boolean existsByIdAndReceiverId(Long id, Long receiverId);
}