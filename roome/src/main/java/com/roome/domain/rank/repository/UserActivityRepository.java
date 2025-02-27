package com.roome.domain.rank.repository;

import com.roome.domain.rank.entity.UserActivity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

  // 특정 기간 이후의 모든 활동 기록 조회
  List<UserActivity> findAllByCreatedAtAfter(LocalDateTime startDate);

  // 특정 날짜 이전의 모든 활동 기록 삭제
  @Modifying
  @Query("DELETE FROM UserActivity ua WHERE ua.createdAt < :date")
  void deleteAllByCreatedAtBefore(@Param("date") LocalDateTime date);
}