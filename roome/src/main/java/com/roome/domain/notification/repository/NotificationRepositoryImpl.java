package com.roome.domain.notification.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.domain.notification.dto.NotificationSearchCondition;
import com.roome.domain.notification.entity.Notification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.roome.domain.notification.entity.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findNotifications(NotificationSearchCondition condition) {
        return queryFactory
                .selectFrom(notification)
                .where(
                        receiverIdEq(condition.getReceiverId()),
                        cursorLt(condition.getCursor()),
                        isReadEq(condition.getIsRead())
                      )
                .orderBy(notification.id.desc())
                .limit(condition.getLimit() + 1)
                .fetch();
    }

    @Override
    @Transactional
    public void deleteOldNotifications(LocalDateTime threshold) {
        queryFactory
                .delete(notification)
                .where(notification.createdAt.before(threshold))
                .execute();
    }

    // 동적 쿼리 조건들
    private BooleanExpression receiverIdEq(Long receiverId) {
        return receiverId != null ? notification.receiverId.eq(receiverId) : null;
    }

    private BooleanExpression cursorLt(Long cursor) {
        return cursor != null ? notification.id.lt(cursor) : null;
    }

    private BooleanExpression isReadEq(Boolean isRead) {
        return isRead != null ? notification.isRead.eq(isRead) : null;
    }
}
