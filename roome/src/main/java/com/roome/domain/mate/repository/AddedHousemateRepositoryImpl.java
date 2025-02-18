package com.roome.domain.mate.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.domain.mate.dto.HousemateInfo;
import com.roome.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.roome.domain.mate.entity.QAddedHousemate.addedHousemate;

@RequiredArgsConstructor
public class AddedHousemateRepositoryImpl implements AddedHousemateRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HousemateInfo> findByUserId(Long userId, String cursor, int limit, String nickname) {
        QUser addedUser = new QUser("addedUser");

        return queryFactory
                .select(Projections.constructor(HousemateInfo.class,
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
    public List<HousemateInfo> findByAddedId(Long addedId, String cursor, int limit, String nickname) {
        QUser addedUser = new QUser("addedUser");

        return queryFactory
                .select(Projections.constructor(HousemateInfo.class,
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

    private BooleanExpression cursorGt(String cursor) {
        return StringUtils.hasText(cursor) ? addedHousemate.id.gt(Long.parseLong(cursor)) : null;
    }

    private BooleanExpression nicknameContains(String nickname, QUser targetUser) {
        return StringUtils.hasText(nickname) ? targetUser.nickname.contains(nickname) : null;
    }
}