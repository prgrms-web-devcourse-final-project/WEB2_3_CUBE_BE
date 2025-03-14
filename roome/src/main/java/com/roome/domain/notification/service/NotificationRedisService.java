package com.roome.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.notification.dto.NotificationWebSocketMessageDto;
import com.roome.domain.notification.entity.NotificationType;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // Redis로 알림 발행 (Publish)
    public void publishNotification(Long receiverId, Long notificationId, NotificationType type) {
        String channelName = "notification:" + receiverId;

        try {
            NotificationWebSocketMessageDto messageDto = NotificationWebSocketMessageDto.of(
                    notificationId, type, receiverId);

            String jsonMessage = objectMapper.writeValueAsString(messageDto);

            log.info("Redis를 통한 알림 메시지 발행: 수신자={}, 알림ID={}, 채널={}",
                    receiverId, notificationId, channelName);

            redisTemplate.convertAndSend(channelName, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("알림 메시지 JSON 변환 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.NOTIFICATION_SENDING_ERROR);
        }
    }

    // Redis에서 수신한 알림을 웹소켓으로 전달
    public void handleNotificationMessage(String message) {
        try {
            NotificationWebSocketMessageDto messageDto = objectMapper.readValue(
                    message, NotificationWebSocketMessageDto.class);

            Long receiverId = messageDto.getReceiverId();

            log.info("Redis에서 알림 메시지 수신: 수신자={}, 알림ID={}",
                    receiverId, messageDto.getNotificationId());

            // 웹소켓을 통해 클라이언트에게 전달
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/notification",
                    messageDto
            );
        } catch (JsonProcessingException e) {
            log.error("수신된 알림 메시지 파싱 실패: {}", e.getMessage());
        }
    }
}