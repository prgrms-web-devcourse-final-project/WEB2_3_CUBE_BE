package com.roome.domain.cdcomment.notificationEvent;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.global.notificationEvent.NotificationEvent;
import lombok.Getter;

@Getter
public class CdCommentCreatedEvent extends NotificationEvent {
    private final Long cdId;
    private final Long commentId;

    public CdCommentCreatedEvent(Object source, Long senderId, Long receiverId, Long cdId, Long commentId) {
        super(source, senderId, receiverId);
        this.cdId = cdId;
        this.commentId = commentId;
    }

    @Override
    public Long getTargetId() {
        return cdId; // CD ID를 타겟으로 설정
    }

    @Override
    public NotificationType getType() {
        return NotificationType.MUSIC_COMMENT;
    }
}
