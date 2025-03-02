package com.roome.domain.point.entity;

import com.roome.domain.point.exception.InsufficientPointsException;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "points")
public class Point {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private int balance; // 현재 보유 포인트

  @Column(nullable = false)
  private int totalEarned; // 누적 적립 포인트

  @Column(nullable = false)
  private int totalUsed; // 누적 사용 포인트

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public Point(User user, int balance, int totalEarned, int totalUsed) {
    this.user = user;
    this.balance = balance;
    this.totalEarned = totalEarned;
    this.totalUsed = totalUsed;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public static Point init(User user, LocalDateTime now) {
    Point point = new Point();
    point.user = user;
    point.balance = 0;
    point.totalEarned = 0;
    point.totalUsed = 0;
    point.createdAt = now;
    point.updatedAt = now;
    return point;
  }

  // 포인트 적립
  public void addPoints(int amount) {
    this.balance += amount;
    this.totalEarned += amount;
    this.updatedAt = LocalDateTime.now();
  }

  // 포인트 사용
  public void subtractPoints(int amount) {
    if (this.balance < amount) {
      throw new InsufficientPointsException();
    }
    this.balance -= amount;
    this.totalUsed += amount;
    this.updatedAt = LocalDateTime.now();
  }

}
