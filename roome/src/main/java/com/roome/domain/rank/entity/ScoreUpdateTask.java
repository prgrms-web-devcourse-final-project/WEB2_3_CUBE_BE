package com.roome.domain.rank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "score_update_tasks")
@Getter
@Setter
@NoArgsConstructor
public class ScoreUpdateTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private int score;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskStatus status;

  @Column(nullable = false)
  private int retryCount;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column
  private LocalDateTime updatedAt;

  public void incrementRetryCount() {
    this.retryCount++;
    this.updatedAt = LocalDateTime.now();
  }

  // 새 task를 위한 생성자
  public ScoreUpdateTask(Long userId, int score) {
    this.userId = userId;
    this.score = score;
    this.status = TaskStatus.PENDING;
    this.retryCount = 0;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }
}