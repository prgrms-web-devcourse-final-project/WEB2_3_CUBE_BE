package com.roome.global.config;

import com.roome.global.jwt.interceptor.JwtWebSocketInterceptor;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 구독 접두사
        config.setApplicationDestinationPrefixes("/app"); // 메시지 전송 접두사
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹소켓 엔드포인트 등록 및 CORS 설정
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "https://desqb38rc2v50.cloudfront.net",
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "*"
                ) // SecurityConfig와 동일한 CORS 설정 사용
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JWT 토큰 검증을 위한 인터셉터 등록
        registration.interceptors(new JwtWebSocketInterceptor(jwtTokenProvider, redisService));
    }
}