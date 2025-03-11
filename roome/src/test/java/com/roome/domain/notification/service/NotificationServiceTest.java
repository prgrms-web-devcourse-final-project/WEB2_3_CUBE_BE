package com.roome.domain.notification.service;

import com.roome.domain.notification.dto.*;
import com.roome.domain.notification.entity.Notification;
import com.roome.domain.notification.entity.NotificationType;
import com.roome.domain.notification.repository.NotificationRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;




@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 생성 성공")
    void createNotification_Success() {
        // given
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                                                                     .senderId(1L)
                                                                     .receiverId(2L)
                                                                     .targetId(3L)
                                                                     .type(NotificationType.GUESTBOOK)
                                                                     .build();

        User sender = User.builder().id(1L).build();
        User receiver = User.builder().id(2L).build();

        // save 후 반환되는 객체에는 id가 설정되어 있을 것임
        Notification savedNotification = Notification.builder()
                                                     .senderId(request.getSenderId())
                                                     .receiverId(request.getReceiverId())
                                                     .targetId(request.getTargetId())
                                                     .type(request.getType())
                                                     .build();
        // Reflection을 사용하여 테스트용 id 설정
        ReflectionTestUtils.setField(savedNotification, "id", 1L);

        when(userRepository.findById(request.getSenderId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(request.getReceiverId())).thenReturn(Optional.of(receiver));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // when
        Long notificationId = notificationService.createNotification(request);

        // then
        assertThat(notificationId).isEqualTo(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void readNotification_Success() {
        // given
        Long notificationId = 1L;
        Long receiverId = 2L;
        Notification notification = createNotification(notificationId, "TestUser");

        when(notificationRepository.existsByIdAndReceiverId(notificationId, receiverId)).thenReturn(true);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when
        NotificationReadResponse response = notificationService.readNotification(notificationId, receiverId);

        // then
        assertThat(response.getType()).isEqualTo(NotificationType.GUESTBOOK);
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotifications_Success() {
        // given
        NotificationSearchCondition condition = NotificationSearchCondition.builder()
                                                                           .limit(2)
                                                                           .cursor(null)
                                                                           .build();

        List<Notification> notifications = Arrays.asList(
                createNotification(3L, "User3"),
                createNotification(2L, "User2"),
                createNotification(1L, "User1")
                                                        );

        when(notificationRepository.findNotifications(condition)).thenReturn(notifications);
        when(userRepository.getById(any())).thenReturn(createUser("TestUser"));

        // when
        NotificationResponse response = notificationService.getNotifications(condition);

        // then
        assertThat(response.getNotifications()).hasSize(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo("2");
    }

    @Test
    @DisplayName("잘못된 limit 값으로 알림 목록 조회 시 예외 발생")
    void getNotifications_WithInvalidLimit_ThrowsException() {
        // given
        NotificationSearchCondition condition = NotificationSearchCondition.builder()
                                                                           .limit(0)
                                                                           .cursor(null)
                                                                           .build();

        // when & then
        assertThrows(BusinessException.class, () ->
                             notificationService.getNotifications(condition)
                    );
    }

    private Notification createNotification(Long id, String senderName) {
        Notification notification = Notification.builder()
                                                .type(NotificationType.GUESTBOOK)
                                                .senderId(id)
                                                .targetId(1L)  // 테스트용 기본값 설정
                                                .receiverId(2L)  // 테스트용 기본값 설정
                                                .build();
        // Reflection을 사용하여 테스트용 id 설정
        ReflectionTestUtils.setField(notification, "id", id);
        return notification;
    }

    private User createUser(String name) {
        return User.builder()
                   .name(name)
                   .profileImage("test.jpg")
                   .build();
    }
}