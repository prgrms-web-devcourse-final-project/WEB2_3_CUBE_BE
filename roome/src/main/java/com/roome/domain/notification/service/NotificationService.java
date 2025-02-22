package com.roome.domain.notification.service;

import com.roome.domain.notification.dto.NotificationInfo;
import com.roome.domain.notification.dto.NotificationReadResponse;
import com.roome.domain.notification.dto.NotificationResponse;
import com.roome.domain.notification.dto.NotificationSearchCondition;
import com.roome.domain.notification.entity.Notification;
import com.roome.domain.notification.repository.NotificationRepository;
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
    private final UserRepository userRepository; // User 정보 조회를 위해 필요

    public NotificationResponse getNotifications(Long receiverId, Long cursor, int limit, boolean isRead) {

        // 검색 조건 생성
        NotificationSearchCondition condition = NotificationSearchCondition.builder()
                                                                           .receiverId(receiverId)
                                                                           .cursor(cursor)
                                                                           .isRead(isRead)
                                                                           .limit(limit)
                                                                           .build();

        // 알림 조회 (limit + 1개를 조회하여 다음 페이지 존재 여부 확인)
        List<Notification> notifications = notificationRepository.findNotifications(condition);

        // hasNext 판단 및 limit 개수만큼 잘라내기
        boolean hasNext = notifications.size() > limit;
        List<Notification> pagedNotifications = hasNext ?
                notifications.subList(0, limit) : notifications;

        // NotificationInfo DTO로 변환
        List<NotificationInfo> notificationInfos = pagedNotifications.stream()
                                                                     .map(this::convertToNotificationInfo)
                                                                     .toList();

        // 다음 커서 설정
        String nextCursor = hasNext && !pagedNotifications.isEmpty() ?
                String.valueOf(pagedNotifications.get(pagedNotifications.size() - 1).getId()) : null;

        return NotificationResponse.builder()
                                   .notifications(notificationInfos)
                                   .nextCursor(nextCursor)
                                   .hasNext(hasNext)
                                   .build();
    }

    @Transactional
    public NotificationReadResponse readNotification(Long notificationId, Long receiverId) {
        // 알림 존재 여부 및 접근 권한 확인
        Notification notification = notificationRepository.findById(notificationId)
                                                          .orElseThrow(() -> new BusinessException(
                                                                  ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiverId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        // 이미 읽은 알림인지 확인
        if (notification.isRead()) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ALREADY_READ);
        }

        // 읽음 처리
        notification.markAsRead();

        return NotificationReadResponse.builder()
                                       .type(notification.getType())
                                       .targetId(notification.getTargetId())
                                       .senderId(notification.getSenderId())
                                       .build();
    }
    // Notification 엔티티를 NotificationInfo DTO로 변환하는 메서드
    private NotificationInfo convertToNotificationInfo(Notification notification) {
        return NotificationInfo.builder()
                               .notificationId(notification.getId())
                               .type(notification.getType())
                               .title(notification.getTitle())
                               .content(notification.getContent())
                               .senderId(notification.getSenderId())
                               .senderName(notification.getSenderName())
                               .senderProfileImage(notification.getSenderProfileImage())
                               .targetId(notification.getTargetId())
                               .isRead(notification.isRead())
                               .createdAt(notification.getCreatedAt())
                               .build();
    }
}