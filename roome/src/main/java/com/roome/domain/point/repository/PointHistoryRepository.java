package com.roome.domain.point.repository;

import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  boolean existsByUserIdAndReasonAndCreatedAtBetween(Long userId, PointReason reason, LocalDateTime start, LocalDateTime end);

  Slice<PointHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  Slice<PointHistory> findByUserAndIdLessThanOrderByCreatedAtDesc(User user, Long cursor,
      Pageable pageable);

  long countByUserId(Long userId);

  @Modifying
  @Transactional
  @Query("DELETE FROM PointHistory p WHERE p.user.id = :userId")
  int deleteByUserId(@Param("userId") Long userId);
}

