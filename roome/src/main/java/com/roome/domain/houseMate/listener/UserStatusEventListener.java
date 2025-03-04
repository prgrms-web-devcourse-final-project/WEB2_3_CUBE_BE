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
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusEventListener {

    private final HousemateRepository housemateRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleUserStatusChanged(UserStatusChangedEvent event) {
        Long changedUserId = event.getUserId();
        Status newStatus = event.getStatus();
        UserStatusDto statusUpdate = new UserStatusDto(changedUserId, newStatus);

        try {
            // 1. 상태가 변경된 사용자를 하우스메이트로 등록한 사용자들(팔로워)에게 알림
            List<Long> followerIds = housemateRepository.findFollowerIdsByAddedId(changedUserId);
            sendStatusUpdateToUsers(changedUserId, newStatus, statusUpdate, followerIds, "팔로워");

            // 2. 상태가 변경된 사용자가 하우스메이트로 등록한 사용자들(팔로잉)에게도 알림
            List<Long> followingIds = housemateRepository.findFollowingIdsByUserId(changedUserId);
            sendStatusUpdateToUsers(changedUserId, newStatus, statusUpdate, followingIds, "팔로잉");
        } catch (BusinessException e) {
            log.error("상태 업데이트 전송 중 비즈니스 예외 발생: {}", e.getErrorCode(), e);
        } catch (Exception e) {
            log.error("상태 업데이트 전송 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }

    private void sendStatusUpdateToUsers(Long changedUserId, Status newStatus, UserStatusDto statusUpdate,
                                         List<Long> recipientIds, String relationshipType) {
        for (Long recipientId : recipientIds) {
            try {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(recipientId),
                        "/queue/status-updates",
                        statusUpdate
                );
                log.info("상태 업데이트 전송: 상태 변경된 사용자={}, {}={}, 상태={}",
                        changedUserId, relationshipType, recipientId, newStatus);
            } catch (MessagingException e) {
                log.error("사용자 {}에게 상태 업데이트 전송 실패: {}", recipientId, e.getMessage());
                throw new BusinessException(ErrorCode.NOTIFICATION_SENDING_ERROR);
            } catch (Exception e) {
                log.error("예상치 못한 오류로 사용자 {}에게 알림 전송 실패: {}", recipientId, e.getMessage());
                throw new BusinessException(ErrorCode.NOTIFICATION_DELIVERY_FAILED);
            }
        }
    }
}