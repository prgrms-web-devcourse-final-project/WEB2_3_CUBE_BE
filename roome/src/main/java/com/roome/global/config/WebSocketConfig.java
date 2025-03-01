package com.roome.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정: 클라이언트가 구독할 주제 접두사 설정
        config.enableSimpleBroker("/notification");
        // 클라이언트가 서버로 메시지를 보낼 때 사용할 접두사 설정
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹소켓 연결 엔드포인트 설정
        // (프론트엔드 서버 포트 5173 허용)
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:5173", "https://desqb38rc2v50.cloudfront.net").withSockJS(); // SockJS 지원 추가 (폴백 메커니즘)
    }
}
