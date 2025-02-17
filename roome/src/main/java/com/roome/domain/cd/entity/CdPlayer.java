package com.roome.domain.cd.entity;

import com.roome.global.entity.BaseEntity;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "cd_player")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CdPlayer extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 사용자가 추가한 CD

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cd_id", nullable = false)
  private Cd cd; // CD 정보

  @Column(nullable = false, updatable = false)
  private LocalDateTime addedAt = LocalDateTime.now(); // 추가한 시간

  public CdPlayer(User user, Cd cd) {
    this.user = user;
    this.cd = cd;
  }
}
