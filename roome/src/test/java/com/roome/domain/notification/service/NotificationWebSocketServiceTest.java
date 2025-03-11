package com.roome.domain.notification.service;

import com.roome.domain.notification.entity.NotificationType;
import com.roome.domain.notification.dto.NotificationWebSocketMessageDto;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationWebSocketService notificationWebSocketService;

    @Test
    @DisplayName("알림 메시지 전송 성공 시나리오")
    void sendNotificationToUser_Success() {
        // Given
        Long receiverId = 123L;
        Long notificationId = 456L;
        NotificationType type = NotificationType.MUSIC_COMMENT;

        doNothing().when(messagingTemplate).convertAndSendToUser(
                anyString(), anyString(), any(NotificationWebSocketMessageDto.class));

        // When
        notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);

        // Then
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/notification"),
                any(NotificationWebSocketMessageDto.class)
        );
    }

    @Test
    @DisplayName("MessageDeliveryException 발생 시 NOTIFICATION_DELIVERY_FAILED 예외 발생")
    void sendNotificationToUser_MessageDeliveryException() {
        // Given
        Long receiverId = 123L;
        Long notificationId = 456L;
        NotificationType type = NotificationType.MUSIC_COMMENT;

        doThrow(new MessageDeliveryException("메시지 전송 실패")).when(messagingTemplate)
                .convertAndSendToUser(anyString(), anyString(), any(NotificationWebSocketMessageDto.class));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);
        });

        assertEquals(ErrorCode.NOTIFICATION_DELIVERY_FAILED, exception.getErrorCode());
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/notification"),
                any(NotificationWebSocketMessageDto.class)
        );
    }

    @Test
    @DisplayName("IllegalStateException 발생 시 NOTIFICATION_BROKER_ERROR 예외 발생")
    void sendNotificationToUser_IllegalStateException() {
        // Given
        Long receiverId = 123L;
        Long notificationId = 456L;
        NotificationType type = NotificationType.MUSIC_COMMENT;

        doThrow(new IllegalStateException("브로커 상태 오류")).when(messagingTemplate)
                .convertAndSendToUser(anyString(), anyString(), any(NotificationWebSocketMessageDto.class));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);
        });

        assertEquals(ErrorCode.NOTIFICATION_BROKER_ERROR, exception.getErrorCode());
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/notification"),
                any(NotificationWebSocketMessageDto.class)
        );
    }

    @Test
    @DisplayName("기타 예외 발생 시 NOTIFICATION_SENDING_ERROR 예외 발생")
    void sendNotificationToUser_OtherException() {
        // Given
        Long receiverId = 123L;
        Long notificationId = 456L;
        NotificationType type = NotificationType.MUSIC_COMMENT;

        doThrow(new RuntimeException("기타 예외 발생")).when(messagingTemplate)
                .convertAndSendToUser(anyString(), anyString(), any(NotificationWebSocketMessageDto.class));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);
        });

        assertEquals(ErrorCode.NOTIFICATION_SENDING_ERROR, exception.getErrorCode());
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/notification"),
                any(NotificationWebSocketMessageDto.class)
        );
    }

    @Test
    @DisplayName("수신자 ID가 null인 경우 NOTIFICATION_INVALID_RECEIVER 예외 발생")
    void sendNotificationToUser_WithNullReceiverId() {
        // Given
        Long receiverId = null;
        Long notificationId = 456L;
        NotificationType type = NotificationType.MUSIC_COMMENT;

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);
        });

        assertEquals(ErrorCode.NOTIFICATION_INVALID_RECEIVER, exception.getErrorCode());

        // messagingTemplate이 호출되지 않아야 함
        verify(messagingTemplate, never()).convertAndSendToUser(
                anyString(), anyString(), any(NotificationWebSocketMessageDto.class));
    }

    @Test
    @DisplayName("알림 ID가 null인 경우 NOTIFICATION_INVALID_ID 예외 발생")
    void sendNotificationToUser_WithNullNotificationId() {
        // Given
        Long receiverId = 123L;
        Long notificationId = null;
        NotificationType type = NotificationType.MUSIC_COMMENT;

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);
        });

        assertEquals(ErrorCode.NOTIFICATION_INVALID_ID, exception.getErrorCode());

        // messagingTemplate이 호출되지 않아야 함
        verify(messagingTemplate, never()).convertAndSendToUser(
                anyString(), anyString(), any(NotificationWebSocketMessageDto.class));
    }

    @Test
    @DisplayName("알림 타입이 null인 경우 NOTIFICATION_INVALID_TYPE 예외 발생")
    void sendNotificationToUser_WithNullType() {
        // Given
        Long receiverId = 123L;
        Long notificationId = 456L;
        NotificationType type = null;

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);
        });

        assertEquals(ErrorCode.NOTIFICATION_INVALID_TYPE, exception.getErrorCode());

        // messagingTemplate이 호출되지 않아야 함
        verify(messagingTemplate, never()).convertAndSendToUser(
                anyString(), anyString(), any(NotificationWebSocketMessageDto.class));
    }

    @Test
    @DisplayName("NotificationWebSocketMessageDto 생성 검증")
    void testNotificationWebSocketMessageDtoCreation() {
        // Given
        Long receiverId = 123L;
        Long notificationId = 456L;
        NotificationType type = NotificationType.MUSIC_COMMENT;

        // When & Then - dto 생성이 정상적으로 이루어지는지 확인
        assertDoesNotThrow(() -> {
            notificationWebSocketService.sendNotificationToUser(receiverId, notificationId, type);
        });

        // 올바른 목적지로 메시지가 전송되었는지 확인
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/notification"),
                any(NotificationWebSocketMessageDto.class)
        );
    }
}