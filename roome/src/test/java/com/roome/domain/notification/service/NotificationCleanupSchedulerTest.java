package com.roome.domain.notification.service;

import com.roome.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupSchedulerTest {

    @InjectMocks
    private NotificationCleanupScheduler notificationCleanupScheduler;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("30일 이전 알림 삭제 스케줄러 테스트")
    void cleanupOldNotifications() {
        // given
        LocalDateTime threshold = LocalDateTime
                .now()
                .minusDays(30);

        // when
        notificationCleanupScheduler.cleanupOldNotifications();

        // then
        verify(notificationRepository, times(1)).deleteOldNotifications(any(LocalDateTime.class));
    }
}