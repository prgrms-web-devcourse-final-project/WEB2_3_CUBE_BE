package com.roome.domain.notification.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationResponse {
    private List<NotificationInfo> notifications;
    private String nextCursor;
    private boolean hasNext;

}