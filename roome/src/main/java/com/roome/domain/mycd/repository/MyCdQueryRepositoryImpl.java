package com.roome.domain.mycd.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.roome.domain.cd.entity.QCd;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.entity.QMyCd;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RequiredArgsConstructor
public class MyCdQueryRepositoryImpl implements MyCdQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<MyCd> searchMyCd(Long userId, String keyword, Long cursor, int size) {
    QMyCd myCd = QMyCd.myCd;
    QCd cd = QCd.cd;

    List<MyCd> result = queryFactory
        .selectFrom(myCd)
        .join(myCd.cd, cd)
        .where(myCd.user.id.eq(userId)
            .and(cursor != null ? myCd.id.gt(cursor) : null)
            .and(keyword != null ? cd.title.containsIgnoreCase(keyword)
                .or(cd.artist.containsIgnoreCase(keyword)) : null))
        .orderBy(myCd.id.asc())
        .limit(size)
        .fetch();

    return new PageImpl<>(result);
  }
}
