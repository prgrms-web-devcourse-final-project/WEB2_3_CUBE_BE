package com.roome.domain.user.listener;

import com.roome.domain.user.entity.Status;
import com.roome.domain.user.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWebSocketEventListener {
    private final UserStatusService userStatusService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        updateUserStatus(headerAccessor, Status.ONLINE, "연결");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        updateUserStatus(headerAccessor, Status.OFFLINE, "연결 해제");
    }

    private void updateUserStatus(StompHeaderAccessor headerAccessor, Status status, String eventType) {
        String sessionId = headerAccessor.getSessionId();
        log.debug("웹소켓 {} 이벤트 감지: sessionId={}", eventType, sessionId);

        // 웹소켓 세션의 인증 객체를 사용
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("웹소켓 {} 이벤트에 Principal 정보 없음: sessionId={}", eventType, sessionId);
            return;
        }

        try {
            // 인터셉터에서 설정한 형식 그대로 사용
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
            Long userId = (Long) authentication.getPrincipal();

            if (userId == null) {
                log.warn("웹소켓 {} 이벤트의 Authentication에서 userId 추출 실패: sessionId={}", eventType, sessionId);
                return;
            }

            log.info("사용자 {} 감지: userId={}, sessionId={}", eventType, userId, sessionId);
            userStatusService.updateUserStatus(userId, status);
        } catch (Exception e) {
            log.error("웹소켓 {} 이벤트 처리 중 오류 발생: sessionId={}", eventType, sessionId, e);
        }
    }
}