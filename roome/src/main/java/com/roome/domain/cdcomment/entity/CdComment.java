package com.roome.domain.cdcomment.entity;

import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CdComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  private MyCd myCd;

  private String timestamp;
  private String content;

  @Column(updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @Builder
  public CdComment(Long id, User user, MyCd myCd, String timestamp, String content) {
    this.id = id;
    this.user = user;
    this.myCd = myCd;
    this.timestamp = timestamp;
    this.content = content;
  }
}
