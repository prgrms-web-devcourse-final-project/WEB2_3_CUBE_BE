package com.roome.domain.cdtemplate.entity;

import com.roome.domain.mycd.entity.MyCd;
import com.roome.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cd_template")
public class CdTemplate extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "my_cd_id", nullable = false, unique = true)
  private MyCd myCd;

  @Column(nullable = false, length = 255)
  private String comment1;

  @Column(nullable = false, length = 255)
  private String comment2;

  @Column(nullable = false, length = 255)
  private String comment3;

  @Column(nullable = false, length = 255)
  private String comment4;

  @Builder
  public CdTemplate(MyCd myCd, String comment1, String comment2, String comment3, String comment4) {
    this.myCd = myCd;
    this.comment1 = comment1;
    this.comment2 = comment2;
    this.comment3 = comment3;
    this.comment4 = comment4;
  }

  public void update(String comment1, String comment2, String comment3, String comment4) {
    this.comment1 = comment1;
    this.comment2 = comment2;
    this.comment3 = comment3;
    this.comment4 = comment4;
  }
}
