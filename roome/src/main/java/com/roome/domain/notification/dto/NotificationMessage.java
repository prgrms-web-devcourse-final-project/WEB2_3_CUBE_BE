package com.roome.domain.notification.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class NotificationMessage {
    private Long timestamp = System.currentTimeMillis();
}
