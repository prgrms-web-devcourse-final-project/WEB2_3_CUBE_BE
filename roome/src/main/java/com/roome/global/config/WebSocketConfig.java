package com.roome.global.config;

import com.roome.global.jwt.interceptor.JwtWebSocketInterceptor;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user")
                .setHeartbeatValue(new long[]{25000, 25000})
                .setTaskScheduler(taskScheduler()); // 서버->클라이언트, 클라이언트->서버 하트비트 주기 설정(밀리초 단위)
        config.setUserDestinationPrefix("/user");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹소켓 엔드포인트 등록 및 CORS 설정
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "https://desqb38rc2v50.cloudfront.net",
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://localhost:63342"
                ) // SecurityConfig와 동일한 CORS 설정 사용
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JWT 토큰 검증을 위한 인터셉터 등록
        registration.interceptors(new JwtWebSocketInterceptor(jwtTokenProvider, redisService));
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 10분(600,000 밀리초) 동안 메시지가 없으면 연결 해제
        registration.setMessageSizeLimit(64 * 1024) // 메시지 크기 제한 (64KB)
                .setSendBufferSizeLimit(512 * 1024) // 버퍼 크기 (512KB)
                .setSendTimeLimit(20000) // 메시지 전송 제한 시간 (20초)
                .setTimeToFirstMessage(60000); // 첫 메시지까지 대기 시간 (60초)
    }

}