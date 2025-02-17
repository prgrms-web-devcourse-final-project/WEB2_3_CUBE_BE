package com.roome.domain.cd.entity;

import com.roome.global.entity.BaseEntity;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cd_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CdComment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cd_player_id", nullable = false)
  private CdPlayer cdPlayer; // 특정 CDPlayer (CD 목록) 에 대한 댓글

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 댓글 작성자

  @Column(nullable = false, length = 500)
  private String comment; // 댓글 내용

  @Column(nullable = true)
  private Integer timestampSec; // 특정 시간 (초 단위, null 가능)

  public CdComment(CdPlayer cdPlayer, User user, String comment, Integer timestampSec) {
    this.cdPlayer = cdPlayer;
    this.user = user;
    this.comment = comment;
    this.timestampSec = timestampSec;
  }
}
