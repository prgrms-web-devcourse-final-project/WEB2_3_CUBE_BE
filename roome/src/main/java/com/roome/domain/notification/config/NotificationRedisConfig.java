package com.roome.domain.notification.config;

import com.roome.domain.notification.service.NotificationRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationRedisConfig {

    private final NotificationRedisService notificationRedisService;

    @Bean
    public MessageListenerAdapter notificationMessageListener() {
        return new MessageListenerAdapter(notificationRedisService, "handleNotificationMessage");
    }

    @Bean
    public RedisMessageListenerContainer notificationListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter notificationMessageListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // notification:* 패턴의 모든 채널 구독
        container.addMessageListener(notificationMessageListener, new PatternTopic("notification:*"));

        log.info("Redis 알림 구독 설정 완료: notification:* 채널");
        return container;
    }
}