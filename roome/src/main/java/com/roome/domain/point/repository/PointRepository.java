package com.roome.domain.point.repository;

import com.roome.domain.point.entity.Point;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

  Optional<Point> findByUserId(Long userId);

  // 원자적 적립: read-modify-write 대신 DB에서 직접 증가시켜 lost update를 방지
  @Modifying
  @Query("""
          UPDATE Point p
          SET p.balance = p.balance + :amount,
              p.totalEarned = p.totalEarned + :amount,
              p.updatedAt = :now
          WHERE p.user.id = :userId
      """)
  int addBalance(@Param("userId") Long userId, @Param("amount") int amount,
      @Param("now") LocalDateTime now);

  // 원자적 차감: 잔액이 충분할 때만 차감하고, 갱신 행 수(0/1)로 성공 여부를 판단
  // 조건 검사와 차감이 단일 UPDATE로 원자 수행되어 확인-차감 사이의 경쟁이 발생하지 않음
  @Modifying
  @Query("""
          UPDATE Point p
          SET p.balance = p.balance - :amount,
              p.totalUsed = p.totalUsed + :amount,
              p.updatedAt = :now
          WHERE p.user.id = :userId AND p.balance >= :amount
      """)
  int subtractBalanceIfEnough(@Param("userId") Long userId, @Param("amount") int amount,
      @Param("now") LocalDateTime now);
}
