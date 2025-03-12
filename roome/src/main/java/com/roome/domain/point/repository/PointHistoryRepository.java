package com.roome.domain.point.repository;

import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

  // 최신순으로 전체 포인트 내역 조회 (처음 조회할 때)
  @Query("""
          SELECT ph FROM PointHistory ph 
          WHERE ph.user.id = :userId 
          ORDER BY ph.createdAt DESC, ph.id DESC
      """)
  Slice<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  // 특정 날짜 이하 && 특정 ID(cursor) 이하의 내역을 최신순으로 조회
  @Query("""
          SELECT ph FROM PointHistory ph 
          WHERE ph.user.id = :userId 
          AND (DATE(ph.createdAt) < :dayCursor 
              OR (DATE(ph.createdAt) = :dayCursor AND ph.id < :itemCursor))
          ORDER BY ph.createdAt DESC, ph.id DESC
      """)
  Slice<PointHistory> findHistoryByCursor(
      @Param("userId") Long userId,
      @Param("dayCursor") LocalDate dayCursor,
      @Param("itemCursor") Long itemCursor,
      Pageable pageable
  );

  // 전체 데이터 개수
  long countByUserId(Long userId);

  // 특정 유저의 전체 포인트 내역 삭제
  @Modifying
  @Transactional
  @Query("DELETE FROM PointHistory p WHERE p.user.id = :userId")
  int deleteByUserId(@Param("userId") Long userId);

  // 오늘 중복 포인트 적립 여부 확인
  @Query("""
          SELECT CASE WHEN COUNT(ph) > 0 THEN TRUE ELSE FALSE END
          FROM PointHistory ph 
          WHERE ph.user.id = :userId 
          AND ph.reason = :reason 
          AND ph.createdAt >= CURRENT_DATE
      """)
  boolean existsRecentEarned(@Param("userId") Long userId, @Param("reason") PointReason reason);

  // 가장 최신 포인트 내역 ID 조회 (firstId)
  @Query("SELECT MAX(ph.id) FROM PointHistory ph WHERE ph.user.id = :userId")
  Long findFirstIdByUser(@Param("userId") Long userId);

  // 가장 오래된 포인트 내역 ID 조회 (lastId)
  @Query("SELECT MIN(ph.id) FROM PointHistory ph WHERE ph.user.id = :userId")
  Long findLastIdByUser(@Param("userId") Long userId);

  // 최근 구매 후 사용한 포인트 여부 확인
  @Query("""
          SELECT CASE WHEN COUNT(ph) > 0 THEN TRUE ELSE FALSE END
          FROM PointHistory ph
          WHERE ph.user.id = :userId
          AND ph.reason = 'POINT_USE'
          AND ph.createdAt > (
              SELECT MAX(ph2.createdAt)
              FROM PointHistory ph2
              WHERE ph2.user.id = :userId AND ph2.reason = 'POINT_PURCHASE'
          )
      """)
  boolean hasUsedPointsAfterLastPurchase(@Param("userId") Long userId);

  // 최근 포인트 구매 내역 조회
  @Query("""
          SELECT ph FROM PointHistory ph
          WHERE ph.user.id = :userId
          AND ph.reason IN :purchaseReasons
          ORDER BY ph.createdAt DESC
      """)
  List<PointHistory> findLatestPurchase(@Param("userId") Long userId,
      @Param("purchaseReasons") List<PointReason> purchaseReasons, Pageable pageable);
}

