package com.roome.domain.cdcomment.entity;

import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseEntity;
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
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 댓글 작성자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "my_cd_id", nullable = false)
  private MyCd myCd; // 댓글이 달린 CD

  @Column(nullable = false)
  private String content; // 댓글 내용

  public CdComment(User user, MyCd myCd, String content) {
    this.user = user;
    this.myCd = myCd;
    this.content = content;
  }
}
