package com.roome.domain.event.service;

import com.roome.domain.event.notificationEvent.EventUpcomingNotificationEvent;
import com.roome.domain.notification.dto.NotificationType;
import com.roome.domain.notification.entity.Notification;
import com.roome.domain.notification.listener.NotificationEventListener;
import com.roome.domain.notification.repository.NotificationRepository;
import com.roome.domain.notification.service.NotificationService;
import com.roome.domain.notification.service.NotificationWebSocketService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("이벤트 알림 스케줄러 테스트")
class EventNotificationSchedulerTest {

    @Autowired
    private EventNotificationScheduler eventNotificationScheduler;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationWebSocketService notificationWebSocketService;

    @MockBean
    private NotificationEventListener notificationEventListener;

    @MockBean
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        // ApplicationEventPublisher를 목으로 대체
        ReflectionTestUtils.setField(eventNotificationScheduler, "eventPublisher", applicationEventPublisher);
    }

    @Test
    @DisplayName("모든 사용자에게 이벤트 알림이 정상적으로 생성되는지 확인")
    void sendEventNotifications_CreatesNotificationsForAllUsers() throws InterruptedException {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        User user1 = User.create(
                "사용자1",
                "닉네임1",
                "user1@example.com",
                "profile1.jpg",
                Provider.GOOGLE,
                "google-id-1",
                now
        );
        user1.setId(1L);

        User user2 = User.create(
                "사용자2",
                "닉네임2",
                "user2@example.com",
                "profile2.jpg",
                Provider.KAKAO,
                "kakao-id-2",
                now
        );
        user2.setId(2L);

        User user3 = User.create(
                "사용자3",
                "닉네임3",
                "user3@example.com",
                "profile3.jpg",
                Provider.NAVER,
                "naver-id-3",
                now
        );
        user3.setId(3L);

        List<User> users = Arrays.asList(user1, user2, user3);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(userPage);

        // 이벤트 발행 시 직접 알림 생성
        doAnswer(invocation -> {
            EventUpcomingNotificationEvent event = invocation.getArgument(0);

            // 발행된 이벤트로부터 알림 생성하여 저장
            Notification notification = Notification.builder()
                    .type(NotificationType.EVENT)
                    .senderId(0L)
                    .receiverId(event.getReceiverId())
                    .targetId(0L).build();
            notificationRepository.save(notification);

            return null;
        }).when(applicationEventPublisher).publishEvent(any(EventUpcomingNotificationEvent.class));

        // Act
        eventNotificationScheduler.sendEventNotifications();

        // 비동기 처리를 위한 대기 시간
        TimeUnit.SECONDS.sleep(1);

        // Assert
        List<Notification> notifications = notificationRepository.findAll();

        // 사용자 수만큼의 알림이 생성되었는지 확인
        assertEquals(3, notifications.size());

        // 각 사용자에 대한 알림이 올바르게 생성되었는지 확인
        for (Notification notification : notifications) {
            assertEquals(NotificationType.EVENT, notification.getType());
            assertEquals(0L, notification.getSenderId());
            assertEquals(0L, notification.getTargetId());

            // 수신자 ID가 유효한지 확인
            Long receiverId = notification.getReceiverId();
            assertTrue(receiverId == 1L || receiverId == 2L || receiverId == 3L);
        }

        // ApplicationEventPublisher가 3번 호출되었는지 확인
        verify(applicationEventPublisher, times(3)).publishEvent(any(EventUpcomingNotificationEvent.class));
    }

    @Test
    @DisplayName("사용자 목록이 비어있을 경우 알림이 생성되지 않는지 확인")
    void sendEventNotifications_EmptyUserList_NoNotificationsCreated() throws InterruptedException {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(List.of());

        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        eventNotificationScheduler.sendEventNotifications();

        // 비동기 처리를 위한 대기 시간
        TimeUnit.SECONDS.sleep(1);

        // Assert
        List<Notification> notifications = notificationRepository.findAll();
        assertTrue(notifications.isEmpty());

        // ApplicationEventPublisher가 호출되지 않았는지 확인
        verify(applicationEventPublisher, never()).publishEvent(any(EventUpcomingNotificationEvent.class));
    }
}