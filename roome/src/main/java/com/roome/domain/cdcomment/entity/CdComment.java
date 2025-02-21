package com.roome.domain.cdcomment.entity;

import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 댓글 작성자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "my_cd_id", nullable = false)
  private MyCd myCd; // 댓글이 달린 CD

  @Column(nullable = false, length = 10)
  private String timestamp; // 댓글이 달린 타임스탬프 (예: "3:40")

  @Column(nullable = false, length = 255)
  private String content; // 댓글 내용

  @Builder
  public CdComment(User user, MyCd myCd, String timestamp, String content) {
    this.user = user;
    this.myCd = myCd;
    this.timestamp = timestamp;
    this.content = content;
  }
}
