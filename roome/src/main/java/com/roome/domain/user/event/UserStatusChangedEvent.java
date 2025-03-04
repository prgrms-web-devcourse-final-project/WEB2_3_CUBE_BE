package com.roome.domain.user.event;

import com.roome.domain.user.entity.Status;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserStatusChangedEvent extends ApplicationEvent {
    private final Long userId;
    private final Status status;

    public UserStatusChangedEvent(Object source, Long userId, Status status) {
        super(source);
        this.userId = userId;
        this.status = status;
    }
}