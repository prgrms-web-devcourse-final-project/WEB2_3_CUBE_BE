package com.roome.domain.notification.service;

import com.roome.domain.notification.dto.*;
import com.roome.domain.notification.entity.Notification;
import com.roome.domain.notification.repository.NotificationRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 알림 생성 서비스
    @Transactional
    public Long createNotification(CreateNotificationRequest request) {
        // 발신자와 수신자 존재 확인
        userRepository.findById(request.getSenderId())
                      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        userRepository.findById(request.getReceiverId())
                      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Notification notification = Notification.builder()
                                                .type(request.getType())
                                                .senderId(request.getSenderId())
                                                .targetId(request.getTargetId())
                                                .receiverId(request.getReceiverId())
                                                .build();

        return notificationRepository.save(notification).getId();
    }

    // 알림 읽음 처리 서비스
    @Transactional
    public NotificationReadResponse readNotification(Long notificationId, Long receiverId) {
        // existsByIdAndReceiverId 활용하여 단일 쿼리로 확인
        if (!notificationRepository.existsByIdAndReceiverId(notificationId, receiverId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        Notification notification = notificationRepository.findById(notificationId)
                                                          .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.isRead()) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ALREADY_READ);
        }

        notification.markAsRead();

        return NotificationReadResponse.builder()
                                       .type(notification.getType())
                                       .targetId(notification.getTargetId())
                                       .senderId(notification.getSenderId())
                                       .build();
    }

    // 알림 목록 조회 서비스
    public NotificationResponse getNotifications(NotificationSearchCondition condition) {
        // 조건 유효성 검사 추가
        if (condition.getLimit() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT_VALUE);
        }

        List<Notification> notifications = notificationRepository.findNotifications(condition);
        boolean hasNext = notifications.size() > condition.getLimit();

        // 페이지 크기에 맞게 리스트 자르기
        List<Notification> pagedNotifications = hasNext ?
                notifications.subList(0, condition.getLimit()) : notifications;

        List<NotificationInfo> notificationInfos = pagedNotifications.stream()
                                                                     .map(this::convertToNotificationInfo)
                                                                     .toList();

        // 마지막 페이지 처리 명확화
        String nextCursor = null;
        if (hasNext && !pagedNotifications.isEmpty()) {
            nextCursor = String.valueOf(pagedNotifications.get(pagedNotifications.size() - 1).getId());
        }

        return NotificationResponse.builder()
                                   .notifications(notificationInfos)
                                   .nextCursor(nextCursor)
                                   .hasNext(hasNext)
                                   .build();
    }

    private NotificationInfo convertToNotificationInfo(Notification notification) {
        User sender = userRepository.getById(notification.getSenderId());  // 가정: Notification 엔티티에 sender 관계가 매핑되어 있음

        return NotificationInfo.builder()
                               .notificationId(notification.getId())
                               .type(notification.getType())
                               .senderId(notification.getSenderId())
                               .senderNickName(sender.getName())
                               .senderProfileImage(sender.getProfileImage())
                               .targetId(notification.getTargetId())
                               .createdAt(notification.getCreatedAt())
                               .build();
    }
}