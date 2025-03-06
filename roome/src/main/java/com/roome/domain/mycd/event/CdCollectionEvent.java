package com.roome.domain.mycd.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class CdCollectionEvent extends ApplicationEvent {
    private final Long userId;

    public CdCollectionEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }

    //CD 추가 이벤트
    public static class CdAddedEvent extends CdCollectionEvent {
        public CdAddedEvent(Object source, Long userId) {
            super(source, userId);
        }
    }

    //CD 제거 이벤트
    public static class CdRemovedEvent extends CdCollectionEvent {
        public CdRemovedEvent(Object source, Long userId) {
            super(source, userId);
        }
    }
}