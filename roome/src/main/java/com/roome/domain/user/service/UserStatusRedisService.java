package com.roome.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.houseMate.dto.UserStatusDto;
import com.roome.domain.user.entity.Status;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // Redis로 상태 변경 이벤트 발행 (Publish)
    public void publishStatusChange(Long userId, Long recipientId, Status status) {
        String channelName = "status-update:" + recipientId;

        try {
            UserStatusDto statusDto = new UserStatusDto(userId, status);
            String jsonMessage = objectMapper.writeValueAsString(statusDto);

            log.info("Redis를 통한 상태 업데이트 발행: 사용자={}, 수신자={}, 상태={}, 채널={}",
                    userId, recipientId, status, channelName);

            redisTemplate.convertAndSend(channelName, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("상태 업데이트 JSON 변환 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.NOTIFICATION_SENDING_ERROR);
        }
    }

    // Redis에서 수신한 상태 업데이트를 웹소켓으로 전달
    public void handleStatusUpdateMessage(String message, String channel) {
        try {
            // 메시지 형식: {"userId": 123, "status": "ONLINE"}
            UserStatusDto statusDto = objectMapper.readValue(message, UserStatusDto.class);

            // 채널 이름에서 수신자 ID 추출 (예: "status-update:456" -> "456")
            String recipientIdStr = channel.substring(channel.lastIndexOf(':') + 1);
            Long recipientId = Long.parseLong(recipientIdStr);

            log.info("Redis에서 상태 업데이트 수신: 사용자={}, 수신자={}, 상태={}",
                    statusDto.getUserId(), recipientId, statusDto.getStatus());

            // 웹소켓을 통해 클라이언트에게 전달
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipientId),
                    "/queue/status-updates",
                    statusDto
            );
        } catch (JsonProcessingException e) {
            log.error("수신된 상태 업데이트 메시지 파싱 실패: {}", e.getMessage());
        } catch (Exception e) {
            log.error("상태 업데이트 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}