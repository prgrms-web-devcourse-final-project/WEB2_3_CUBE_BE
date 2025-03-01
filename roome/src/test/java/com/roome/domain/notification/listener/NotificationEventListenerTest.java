package com.roome.domain.notification.listener;

import com.roome.domain.cdcomment.notificationEvent.CdCommentCreatedEvent;
import com.roome.domain.guestbook.notificationEvent.GuestBookCreatedEvent;
import com.roome.domain.houseMate.notificationEvent.HouseMateCreatedEvent;
import com.roome.domain.notification.dto.CreateNotificationRequest;
import com.roome.domain.notification.dto.NotificationType;
import com.roome.domain.notification.service.NotificationService;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.notificationEvent.NotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    @Test
    @DisplayName("알림 이벤트 처리가 성공적으로 이루어져야 한다")
    void handleNotificationEvent_Success() {
        // Given
        NotificationEvent mockEvent = mock(NotificationEvent.class);
        when(mockEvent.getType()).thenReturn(NotificationType.MUSIC_COMMENT);
        when(mockEvent.getSenderId()).thenReturn(1L);
        when(mockEvent.getReceiverId()).thenReturn(2L);
        when(mockEvent.getTargetId()).thenReturn(3L);

        when(notificationService.createNotification(any(CreateNotificationRequest.class))).thenReturn(1L);

        // When
        notificationEventListener.handleNotificationEvent(mockEvent);

        // Then
        verify(notificationService, times(1)).createNotification(any(CreateNotificationRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 데이터로 알림 이벤트를 처리할 경우 예외가 발생해야 한다")
    void handleNotificationEvent_WithInvalidData_ThrowsException() {
        // Given
        NotificationEvent mockEvent = mock(NotificationEvent.class);
        when(mockEvent.getType()).thenReturn(null);
        when(mockEvent.getSenderId()).thenReturn(1L);
        when(mockEvent.getReceiverId()).thenReturn(2L);

        // When & Then
        assertThrows(BusinessException.class, () -> notificationEventListener.handleNotificationEvent(mockEvent));
        verify(notificationService, never()).createNotification(any(CreateNotificationRequest.class));
    }

    @Test
    @DisplayName("알림 서비스에서 예외가 발생할 경우 비즈니스 예외로 변환되어야 한다")
    void handleNotificationEvent_WithServiceException_ThrowsBusinessException() {
        // Given
        NotificationEvent mockEvent = mock(NotificationEvent.class);
        when(mockEvent.getType()).thenReturn(NotificationType.MUSIC_COMMENT);
        when(mockEvent.getSenderId()).thenReturn(1L);
        when(mockEvent.getReceiverId()).thenReturn(2L);
        when(mockEvent.getTargetId()).thenReturn(3L);

        when(notificationService.createNotification(any())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(BusinessException.class, () -> notificationEventListener.handleNotificationEvent(mockEvent));
    }

    @Test
    @DisplayName("CD 댓글 생성 이벤트 처리가 성공적으로 이루어져야 한다")
    void handleCdCommentCreated_Success() {
        // Given
        Object source = new Object();
        CdCommentCreatedEvent event = new CdCommentCreatedEvent(
                source, 1L, 2L, 3L, 4L);

        when(notificationService.createNotification(any())).thenReturn(1L);

        // When
        notificationEventListener.handleCdCommentCreated(event);

        // Then
        verify(notificationService, times(1)).createNotification(any());
    }

    @Test
    @DisplayName("방명록 생성 이벤트 처리가 성공적으로 이루어져야 한다")
    void handleGuestBookCreated_Success() {
        // Given
        Object source = new Object();
        GuestBookCreatedEvent event = new GuestBookCreatedEvent(
                source, 1L, 2L, 3L);

        when(notificationService.createNotification(any())).thenReturn(1L);

        // When
        notificationEventListener.handleGuestBookCreated(event);

        // Then
        verify(notificationService, times(1)).createNotification(any());
    }

    @Test
    @DisplayName("하우스메이트 생성 이벤트 처리가 성공적으로 이루어져야 한다")
    void handleHouseMateCreated_Success() {
        // Given
        Object source = new Object();
        HouseMateCreatedEvent event = new HouseMateCreatedEvent(
                source, 1L, 2L, 3L);

        when(notificationService.createNotification(any())).thenReturn(1L);

        // When
        notificationEventListener.handleHouseMateCreated(event);

        // Then
        verify(notificationService, times(1)).createNotification(any());
    }

    @Test
    @DisplayName("CD 댓글 생성 이벤트 처리 중 예외가 발생하면 비즈니스 예외로 변환되어야 한다")
    void handleCdCommentCreated_WithException() {
        // Given
        Object source = new Object();
        CdCommentCreatedEvent event = new CdCommentCreatedEvent(
                source, 1L, 2L, 3L, 4L);

        when(notificationService.createNotification(any())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(BusinessException.class, () -> notificationEventListener.handleCdCommentCreated(event));
    }

    @Test
    @DisplayName("방명록 생성 이벤트 처리 중 예외가 발생하면 비즈니스 예외로 변환되어야 한다")
    void handleGuestBookCreated_WithException() {
        // Given
        Object source = new Object();
        GuestBookCreatedEvent event = new GuestBookCreatedEvent(
                source, 1L, 2L, 3L);

        when(notificationService.createNotification(any())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(BusinessException.class, () -> notificationEventListener.handleGuestBookCreated(event));
    }

    @Test
    @DisplayName("하우스메이트 생성 이벤트 처리 중 예외가 발생하면 비즈니스 예외로 변환되어야 한다")
    void handleHouseMateCreated_WithException() {
        // Given
        Object source = new Object();
        HouseMateCreatedEvent event = new HouseMateCreatedEvent(
                source, 1L, 2L, 3L);

        when(notificationService.createNotification(any())).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(BusinessException.class, () -> notificationEventListener.handleHouseMateCreated(event));
    }

    // 추가 테스트
    @Test
    @DisplayName("방명록 생성 이벤트의 targetId가 올바르게 전달되는지 검증한다")
    void handleGuestBookCreated_VerifyTargetId() {
        // Given
        Object source = new Object();
        Long guestbookId = 3L;
        GuestBookCreatedEvent event = new GuestBookCreatedEvent(
                source, 1L, 2L, guestbookId);

        when(notificationService.createNotification(any())).thenReturn(1L);

        // When
        notificationEventListener.handleGuestBookCreated(event);

        // Then
        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createNotification(captor.capture());

        CreateNotificationRequest capturedRequest = captor.getValue();
        assertEquals(guestbookId, capturedRequest.getTargetId());
        assertEquals(NotificationType.GUESTBOOK, capturedRequest.getType());
    }
}