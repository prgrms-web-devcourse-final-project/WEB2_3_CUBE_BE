package com.roome.domain.cdtemplate.entity;

import com.roome.domain.mycd.entity.MyCd;
import com.roome.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cd_template")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CdTemplate extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "my_cd_id", nullable = false)
  private MyCd myCd; // 템플릿이 달린 CD

  @Column(nullable = false)
  private String comment1;

  @Column(nullable = false)
  private String comment2;

  @Column(nullable = false)
  private String comment3;

  @Column(nullable = false)
  private String comment4;

  public CdTemplate(MyCd myCd, String comment1, String comment2, String comment3, String comment4) {
    this.myCd = myCd;
    this.comment1 = comment1;
    this.comment2 = comment2;
    this.comment3 = comment3;
    this.comment4 = comment4;
  }

  public void updateTemplate(String comment1, String comment2, String comment3, String comment4) {
    if (comment1 != null) this.comment1 = comment1;
    if (comment2 != null) this.comment2 = comment2;
    if (comment3 != null) this.comment3 = comment3;
    if (comment4 != null) this.comment4 = comment4;
  }
}
