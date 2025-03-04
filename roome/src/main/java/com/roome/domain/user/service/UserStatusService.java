package com.roome.domain.user.service;

import com.roome.domain.user.entity.Status;
import com.roome.domain.user.event.UserStatusChangedEvent;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateUserStatus(Long userId, Status status) {
        log.info("사용자 상태 업데이트 요청: userId={}, status={}", userId, status);

        userRepository.findById(userId).ifPresentOrElse(user -> {
            // 상태가 변경된 경우에만 이벤트 발행
            if (!user.getStatus().equals(status)) {
                log.info("사용자 상태 변경: userId={}, oldStatus={}, newStatus={}", userId, user.getStatus(), status);
                user.updateStatus(status);
                userRepository.save(user);

                // 이벤트 발행
                log.debug("UserStatusChangedEvent 발행: userId={}, newStatus={}", userId, status);
                eventPublisher.publishEvent(new UserStatusChangedEvent(this, userId, status));
                log.info("사용자 상태 업데이트 완료: userId={}", userId);
            } else {
                log.info("사용자 상태가 동일하여 업데이트하지 않음: userId={}, status={}", userId, status);
            }
        }, () -> {
            log.warn("사용자를 찾을 수 없음: userId={}", userId);
        });
    }
}