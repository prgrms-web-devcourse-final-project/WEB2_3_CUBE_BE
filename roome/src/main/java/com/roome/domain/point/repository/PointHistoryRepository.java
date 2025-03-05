package com.roome.domain.point.repository;

import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

  // 최신순 (id 기준) 내역 조회
  Slice<PointHistory> findByUserOrderByIdDesc(User user, Pageable pageable);

  // 특정 id(cursor) 이전의 내역을 최신순으로 조회
  Slice<PointHistory> findByUserAndIdLessThanOrderByIdDesc(User user, Long cursor,
      Pageable pageable);

  long countByUserId(Long userId);

  @Modifying
  @Transactional
  @Query("DELETE FROM PointHistory p WHERE p.user.id = :userId")
  int deleteByUserId(@Param("userId") Long userId);

  // 중복 포인트 적립 여부 확인 (당일 기준)
  @Query("SELECT CASE WHEN COUNT(ph) > 0 THEN TRUE ELSE FALSE END " +
      "FROM PointHistory ph WHERE ph.user.id = :userId " +
      "AND ph.reason = :reason " +
      "AND ph.createdAt >= CURRENT_DATE")
  boolean existsRecentEarned(@Param("userId") Long userId, @Param("reason") PointReason reason);

  @Query("SELECT MAX(ph.id) FROM PointHistory ph WHERE ph.user.id = :userId")
  Long findFirstIdByUser(@Param("userId") Long userId);

  @Query("SELECT MIN(ph.id) FROM PointHistory ph WHERE ph.user.id = :userId")
  Long findLastIdByUser(@Param("userId") Long userId);

}
