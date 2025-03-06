package com.roome.domain.mybook.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class BookCollectionEvent extends ApplicationEvent {
    private final Long userId;

    public BookCollectionEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }

    // 책 추가 이벤트
    public static class BookAddedEvent extends BookCollectionEvent {
        public BookAddedEvent(Object source, Long userId) {
            super(source, userId);
        }
    }

    // 책 삭제 이벤트
    public static class BookRemovedEvent extends BookCollectionEvent {
        public BookRemovedEvent(Object source, Long userId) {
            super(source, userId);
        }
    }
}