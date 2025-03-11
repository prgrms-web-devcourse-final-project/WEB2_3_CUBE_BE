package com.roome.domain.houseMate.listener;

import com.roome.domain.houseMate.dto.UserStatusDto;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.event.UserStatusChangedEvent;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusEventListener {

    private final HousemateRepository housemateRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Async("statusUpdateTaskExecutor")
    @EventListener
    @Transactional(readOnly = true)
    public void handleUserStatusChanged(UserStatusChangedEvent event) {
        Long changedUserId = event.getUserId();
        Status newStatus = event.getStatus();
        UserStatusDto statusUpdate = new UserStatusDto(changedUserId, newStatus);

        try {
            // 1. 팔로워들에게 상태 업데이트 전송
            List<Long> followerIds = housemateRepository.findFollowerIdsByAddedId(changedUserId);
            for (Long followerId : followerIds) {
                sendStatusUpdate(followerId, statusUpdate);
                log.info("상태 업데이트 전송: 사용자={}, 팔로워={}, 상태={}", changedUserId, followerId, newStatus);
            }

            // 2. 팔로잉들에게 상태 업데이트 전송
            List<Long> followingIds = housemateRepository.findFollowingIdsByUserId(changedUserId);
            for (Long followingId : followingIds) {
                sendStatusUpdate(followingId, statusUpdate);
                log.info("상태 업데이트 전송: 사용자={}, 팔로잉={}, 상태={}", changedUserId, followingId, newStatus);
            }
        } catch (Exception e) {
            log.error("상태 업데이트 전송 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }

    private void sendStatusUpdate(Long recipientId, UserStatusDto statusUpdate) {
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(recipientId),
                    "/queue/status-updates",
                    statusUpdate
            );
        } catch (Exception e) {
            log.error("사용자 {}에게 상태 업데이트 전송 실패: {}", recipientId, e.getMessage());
            throw new BusinessException(ErrorCode.NOTIFICATION_SENDING_ERROR);
        }
    }
}