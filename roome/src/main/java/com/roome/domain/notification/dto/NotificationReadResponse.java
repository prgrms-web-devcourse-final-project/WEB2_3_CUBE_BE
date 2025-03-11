package com.roome.domain.notification.dto;

import com.roome.domain.notification.entity.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadResponse {
    private NotificationType type;    // GUESTBOOK, MUSIC_COMMENT, EVENT
    private Long targetId;          // 이동할 페이지의 ID (방명록 ID, 음악 댓글 ID 등)
    private Long senderId;          // 알림을 발생시킨 사용자 ID
}

