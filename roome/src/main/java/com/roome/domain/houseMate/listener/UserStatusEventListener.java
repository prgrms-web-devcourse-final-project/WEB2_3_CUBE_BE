package com.roome.domain.houseMate.listener;

import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.event.UserStatusChangedEvent;
import com.roome.domain.user.service.UserStatusRedisService;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusEventListener {

    private final HousemateRepository housemateRepository;
    private final UserStatusRedisService userStatusRedisService;

    @Async("statusUpdateTaskExecutor")
    @EventListener
    @Transactional(readOnly = true)
    public void handleUserStatusChanged(UserStatusChangedEvent event) {
        Long changedUserId = event.getUserId();
        Status newStatus = event.getStatus();

        try {
            // 1. 팔로워들에게 상태 업데이트 전송
            List<Long> followerIds = housemateRepository.findFollowerIdsByAddedId(changedUserId);
            for (Long followerId : followerIds) {
                userStatusRedisService.publishStatusChange(changedUserId, followerId, newStatus);
                log.info("상태 업데이트 발행: 사용자={}, 팔로워={}, 상태={}", changedUserId, followerId, newStatus);
            }

            // 2. 팔로잉들에게 상태 업데이트 전송
            List<Long> followingIds = housemateRepository.findFollowingIdsByUserId(changedUserId);
            for (Long followingId : followingIds) {
                userStatusRedisService.publishStatusChange(changedUserId, followingId, newStatus);
                log.info("상태 업데이트 발행: 사용자={}, 팔로잉={}, 상태={}", changedUserId, followingId, newStatus);
            }
        } catch (Exception e) {
            log.error("상태 업데이트 전송 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOTIFICATION_EVENT_PROCESSING_FAILED);
        }
    }
}