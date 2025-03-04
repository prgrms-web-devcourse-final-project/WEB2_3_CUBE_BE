package com.roome.domain.point.entity;

import com.roome.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_history")
public class PointHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private int amount; // 변동된 포인트 양 (양수: 적립, 음수: 사용)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PointReason reason; // 적립/사용 사유

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public PointHistory(User user, int amount, PointReason reason) {
    this.user = user;
    this.amount = amount;
    this.reason = reason;
    this.createdAt = LocalDateTime.now(); // 기본적으로 현재 시간 적용
  }

  // 테스트에서 사용하기 위한 생성자 추가
  public PointHistory(User user, int amount, PointReason reason, LocalDateTime createdAt) {
    this.user = user;
    this.amount = amount;
    this.reason = reason;
    this.createdAt = createdAt;
  }
}
