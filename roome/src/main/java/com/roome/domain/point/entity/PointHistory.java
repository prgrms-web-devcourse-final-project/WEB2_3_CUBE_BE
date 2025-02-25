package com.roome.domain.point.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_history")
public class PointHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private int amount; // 변동된 포인트 양 (양수: 적립, 음수: 사용)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PointReason reason; // 적립/사용 사유

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public PointHistory(Long userId, int amount, PointReason reason) {
    this.userId = userId;
    this.amount = amount;
    this.reason = reason;
    this.createdAt = LocalDateTime.now();
  }
}
