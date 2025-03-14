package com.roome.domain.user.config;

import com.roome.domain.user.service.UserStatusRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserStatusRedisConfig {

    private final UserStatusRedisService userStatusRedisService;

    @Bean
    public MessageListenerAdapter statusUpdateMessageListener() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(userStatusRedisService, "handleStatusUpdateMessage");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }

    @Bean
    public RedisMessageListenerContainer statusUpdateListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter statusUpdateMessageListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // status-update:* 패턴의 모든 채널 구독
        container.addMessageListener(statusUpdateMessageListener, new PatternTopic("status-update:*"));

        log.info("Redis 상태 업데이트 구독 설정 완료: status-update:* 채널");
        return container;
    }
}