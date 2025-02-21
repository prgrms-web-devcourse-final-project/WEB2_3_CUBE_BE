package com.roome.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSearchCondition {
    private Long receiverId;
    private Long cursor;
    private Boolean isRead;
    private Integer limit;
}
