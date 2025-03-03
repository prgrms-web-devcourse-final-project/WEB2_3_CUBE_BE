package com.roome.global.jwt.interceptor;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 웹소켓 연결 요청일 때만 인증 처리
            String token = extractTokenFromHeader(accessor);

            if (token != null) {
                // 블랙리스트 체크
                if (redisService.isBlacklisted(token)) {
                    log.warn("[WebSocket 연결 거부] 블랙리스트에 등록된 토큰");
                    throw new BusinessException(ErrorCode.WEBSOCKET_TOKEN_BLACKLISTED);
                }

                // 토큰 검증
                if (jwtTokenProvider.validateAccessToken(token)) {
                    String userIdStr = jwtTokenProvider.getUserIdFromToken(token);

                    try {
                        Long userId = Long.valueOf(userIdStr);

                        // SecurityContext에 인증 정보 설정 (JwtAuthenticationFilter와 유사)
                        Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        // accessor가 mutable인 경우에만 User 설정
                        if (accessor.isMutable()) {
                            accessor.setUser(auth);
                        }

                        log.info("[WebSocket 연결 성공] 사용자 ID: {}", userId);
                    } catch (NumberFormatException e) {
                        log.warn("[WebSocket 연결 거부] 토큰의 userId 형식이 올바르지 않음");
                        throw new BusinessException(ErrorCode.INVALID_USER_ID_FORMAT);
                    }
                } else {
                    log.warn("[WebSocket 연결 거부] 유효하지 않은 토큰");
                    throw new BusinessException(ErrorCode.WEBSOCKET_TOKEN_INVALID);
                }
            } else {
                log.warn("[WebSocket 연결 거부] 토큰 없음");
                throw new BusinessException(ErrorCode.WEBSOCKET_TOKEN_MISSING);
            }
        }

        return message;
    }

    private String extractTokenFromHeader(StompHeaderAccessor accessor) {
        // STOMP 헤더에서 JWT 토큰 추출
        String bearerToken = null;

        if (accessor.getNativeHeader("Authorization") != null && !accessor.getNativeHeader("Authorization").isEmpty()) {
            bearerToken = accessor.getNativeHeader("Authorization").get(0);
        }

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}