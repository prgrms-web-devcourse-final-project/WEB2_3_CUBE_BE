package com.roome.global.jwt.interceptor;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtWebSocketInterceptorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisService redisService;

    @Mock
    private MessageChannel channel;

    private JwtWebSocketInterceptor interceptor;

    @BeforeEach
    public void setup() {
        interceptor = new JwtWebSocketInterceptor(jwtTokenProvider, redisService);
        SecurityContextHolder.clearContext(); // 각 테스트 전에 SecurityContext 초기화
    }

    private Message<?> createConnectMessage(String token) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);

        if (token != null) {
            accessor.setNativeHeader("Authorization", "Bearer " + token);
        }

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("유효한 토큰이 제공되면 인증이 성공해야 한다")
    public void testValidTokenShouldAuthenticate() {
        // Given
        String validToken = "valid_token";
        String userId = "1";
        Message<?> message = createConnectMessage(validToken);

        when(redisService.isBlacklisted(validToken)).thenReturn(false);
        when(jwtTokenProvider.validateAccessToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);

        // When
        Message<?> result = interceptor.preSend(message, channel);

        // Then
        assertNotNull(result);
        // 이제 SecurityContext에서 인증 정보를 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(1L, auth.getPrincipal());
    }

    @Test
    @DisplayName("토큰이 없으면 예외가 발생해야 한다")
    public void testMissingTokenShouldThrowException() {
        // Given
        Message<?> message = createConnectMessage(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            interceptor.preSend(message, channel);
        });

        assertEquals(ErrorCode.WEBSOCKET_TOKEN_MISSING, exception.getErrorCode());
    }

    @Test
    @DisplayName("블랙리스트에 등록된 토큰이면 예외가 발생해야 한다")
    public void testBlacklistedTokenShouldThrowException() {
        // Given
        String blacklistedToken = "blacklisted_token";
        Message<?> message = createConnectMessage(blacklistedToken);

        when(redisService.isBlacklisted(blacklistedToken)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            interceptor.preSend(message, channel);
        });

        assertEquals(ErrorCode.WEBSOCKET_TOKEN_BLACKLISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 예외가 발생해야 한다")
    public void testInvalidTokenShouldThrowException() {
        // Given
        String invalidToken = "invalid_token";
        Message<?> message = createConnectMessage(invalidToken);

        when(redisService.isBlacklisted(invalidToken)).thenReturn(false);
        when(jwtTokenProvider.validateAccessToken(invalidToken)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            interceptor.preSend(message, channel);
        });

        assertEquals(ErrorCode.WEBSOCKET_TOKEN_INVALID, exception.getErrorCode());
    }

    @Test
    @DisplayName("토큰의 UserId 형식이 잘못되면 예외가 발생해야 한다")
    public void testInvalidUserIdFormatShouldThrowException() {
        // Given
        String validToken = "valid_token";
        String invalidUserId = "invalid_user_id"; // 숫자가 아닌 문자열
        Message<?> message = createConnectMessage(validToken);

        when(redisService.isBlacklisted(validToken)).thenReturn(false);
        when(jwtTokenProvider.validateAccessToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(invalidUserId);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            interceptor.preSend(message, channel);
        });

        assertEquals(ErrorCode.INVALID_USER_ID_FORMAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("CONNECT가 아닌 다른 명령에서는 인증을 수행하지 않아야 한다")
    public void testNonConnectCommandShouldNotAttemptAuthentication() {
        // Given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // When
        Message<?> result = interceptor.preSend(message, channel);

        // Then
        assertNotNull(result);
        // 어떤 토큰 검증 메서드도 호출되지 않음
    }
}