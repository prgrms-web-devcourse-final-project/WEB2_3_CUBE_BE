package com.roome.domain.houseMate.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.domain.houseMate.dto.HousemateInfo;
import com.roome.domain.houseMate.entity.QAddedHousemate;
import com.roome.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.roome.domain.houseMate.entity.QAddedHousemate.addedHousemate;

@RequiredArgsConstructor
public class HousemateRepositoryImpl implements HousemateRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HousemateInfo> findByUserId(Long userId, Long cursor, int limit, String nickname) {
        QUser addedUser = new QUser("addedUser");

        return queryFactory
                .select(Projections.constructor(HousemateInfo.class,
                                                addedHousemate.id,
                                                addedUser.id,
                                                addedUser.nickname,
                                                addedUser.profileImage,
                                                addedUser.bio,
                                                addedUser.status))
                .from(addedHousemate)
                .join(addedUser).on(addedHousemate.addedId.eq(addedUser.id))
                .where(
                        addedHousemate.userId.eq(userId),
                        cursorGt(cursor),
                        nicknameContains(nickname, addedUser)
                      )
                .orderBy(addedHousemate.id.asc())
                .limit(limit + 1)
                .fetch();
    }

    @Override
    public List<HousemateInfo> findByAddedId(Long addedId, Long cursor, int limit, String nickname) {
        QUser addedUser = new QUser("addedUser");

        return queryFactory
                .select(Projections.constructor(HousemateInfo.class,
                                                addedHousemate.id,
                                                addedUser.id,
                                                addedUser.nickname,
                                                addedUser.profileImage,
                                                addedUser.bio,
                                                addedUser.status))
                .from(addedHousemate)
                .join(addedUser).on(addedHousemate.userId.eq(addedUser.id))  // 조인 조건만 변경
                .where(
                        addedHousemate.addedId.eq(addedId),  // where 조건만 변경
                        cursorGt(cursor),
                        nicknameContains(nickname, addedUser)
                      )
                .orderBy(addedHousemate.id.asc())
                .limit(limit + 1)
                .fetch();
    }

    @Override
    public List<Long> findFollowerIdsByAddedId(Long addedId) {
        QAddedHousemate addedHousemate = QAddedHousemate.addedHousemate;
        return queryFactory.select(addedHousemate.userId)
                .from(addedHousemate)
                .where(addedHousemate.addedId.eq(addedId))
                .fetch();
    }

    @Override
    public List<Long> findFollowingIdsByUserId(Long userId) {
        QAddedHousemate addedHousemate = QAddedHousemate.addedHousemate;

        return queryFactory
                .select(addedHousemate.addedId)
                .from(addedHousemate)
                .where(addedHousemate.userId.eq(userId))
                .fetch();
    }

    private BooleanExpression cursorGt(Long cursor) {
        return cursor != null ? addedHousemate.id.gt(cursor) : null;
    }

    private BooleanExpression nicknameContains(String nickname, QUser targetUser) {
        return StringUtils.hasText(nickname) ? targetUser.nickname.contains(nickname) : null;
    }
}