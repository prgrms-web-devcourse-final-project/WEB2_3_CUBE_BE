package com.roome.domain.user.listener;

import com.roome.domain.user.entity.Status;
import com.roome.domain.user.service.UserStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserWebSocketEventListenerTest {

    @Mock
    private UserStatusService userStatusService;

    @Mock
    private SessionConnectedEvent connectEvent;

    @Mock
    private SessionDisconnectEvent disconnectEvent;

    private UserWebSocketEventListener listener;
    private StompHeaderAccessor headerAccessor;
    private Message<byte[]> message;
    private final Long userId = 123L;

    @BeforeEach
    void setUp() {
        listener = new UserWebSocketEventListener(userStatusService);

        // StompHeaderAccessor 올바르게 생성
        headerAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        headerAccessor.setSessionId("test-session-id");

        // Principal 객체 생성 (UsernamePasswordAuthenticationToken)
        Principal principal = new UsernamePasswordAuthenticationToken(
                userId, null, Collections.emptyList());
        headerAccessor.setUser(principal);

        // Message 객체 생성
        message = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
    }

    @Test
    @DisplayName("웹소켓 연결 시 사용자 상태가 ONLINE으로 업데이트 되어야 한다")
    void handleWebSocketConnectListener_ShouldUpdateUserStatusToOnline() {
        // Given
        when(connectEvent.getMessage()).thenReturn(message);

        // When
        listener.handleWebSocketConnectListener(connectEvent);

        // Then
        verify(userStatusService, times(1)).updateUserStatus(userId, Status.ONLINE);
    }

    @Test
    @DisplayName("웹소켓 연결 해제 시 사용자 상태가 OFFLINE으로 업데이트 되어야 한다")
    void handleWebSocketDisconnectListener_ShouldUpdateUserStatusToOffline() {
        // Given
        when(disconnectEvent.getMessage()).thenReturn(message);

        // When
        listener.handleWebSocketDisconnectListener(disconnectEvent);

        // Then
        verify(userStatusService, times(1)).updateUserStatus(userId, Status.OFFLINE);
    }

    @Test
    @DisplayName("Principal 정보가 없는 경우 상태 업데이트를 수행하지 않아야 한다")
    void updateUserStatus_NoPrincipal_ShouldNotUpdateStatus() {
        // Given
        StompHeaderAccessor emptyAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        emptyAccessor.setSessionId("test-session-without-principal");
        Message<byte[]> emptyMessage = MessageBuilder.createMessage(
                new byte[0], emptyAccessor.getMessageHeaders());

        when(connectEvent.getMessage()).thenReturn(emptyMessage);

        // When
        listener.handleWebSocketConnectListener(connectEvent);

        // Then
        verify(userStatusService, never()).updateUserStatus(any(), any());
    }

    @Test
    @DisplayName("Principal이 UsernamePasswordAuthenticationToken 타입이 아닌 경우 예외가 발생해도 처리되어야 한다")
    void updateUserStatus_WrongPrincipalType_ShouldHandleException() {
        // Given
        StompHeaderAccessor wrongAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        wrongAccessor.setSessionId("test-session-wrong-principal");

        // 다른 타입의 Principal 설정
        Principal wrongPrincipal = () -> "wrong-principal";
        wrongAccessor.setUser(wrongPrincipal);

        Message<byte[]> wrongMessage = MessageBuilder.createMessage(
                new byte[0], wrongAccessor.getMessageHeaders());

        when(connectEvent.getMessage()).thenReturn(wrongMessage);

        // When
        listener.handleWebSocketConnectListener(connectEvent);

        // Then
        verify(userStatusService, never()).updateUserStatus(any(), any());
    }

    @Test
    @DisplayName("Principal의 값이 null인 경우 상태 업데이트를 수행하지 않아야 한다")
    void updateUserStatus_NullPrincipalValue_ShouldNotUpdateStatus() {
        // Given
        StompHeaderAccessor nullPrincipalAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        nullPrincipalAccessor.setSessionId("test-session-null-principal-value");

        // Principal 값이 null인 인증 객체 생성
        Principal nullPrincipal = new UsernamePasswordAuthenticationToken(
                null, null, Collections.emptyList());
        nullPrincipalAccessor.setUser(nullPrincipal);

        Message<byte[]> nullPrincipalMessage = MessageBuilder.createMessage(
                new byte[0], nullPrincipalAccessor.getMessageHeaders());

        when(connectEvent.getMessage()).thenReturn(nullPrincipalMessage);

        // When
        listener.handleWebSocketConnectListener(connectEvent);

        // Then
        verify(userStatusService, never()).updateUserStatus(any(), any());
    }
}