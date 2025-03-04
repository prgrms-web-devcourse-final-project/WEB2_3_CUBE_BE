package com.roome.domain.point.repository;

import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

  boolean existsByUserIdAndReasonAndCreatedAt(Long userId, PointReason reason, LocalDate createdAt);

  @Modifying
  @Transactional
  @Query("DELETE FROM PointHistory p WHERE p.user.id = :userId")
  int deleteByUserId(@Param("userId") Long userId);
}
