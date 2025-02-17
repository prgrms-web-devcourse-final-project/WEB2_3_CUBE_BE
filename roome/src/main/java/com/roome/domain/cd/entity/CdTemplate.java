package com.roome.domain.cd.entity;

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
  @JoinColumn(name = "cd_player_id", nullable = false)
  private CdPlayer cdPlayer; // 사용자가 추가한 CD (1:1 관계)

  @Column(nullable = false, length = 500)
  private String reason; // CD를 듣게 된 계기

  @Column(nullable = false, length = 500)
  private String bestPart; // 가장 좋았던 부분

  @Column(nullable = false, length = 500)
  private String emotion; // 들으면서 느낀 감정

  @Column(nullable = false, length = 500)
  private String frequentSituation; // 자주 듣는 상황

  public CdTemplate(CdPlayer cdPlayer, String reason, String bestPart, String emotion, String frequentSituation) {
    this.cdPlayer = cdPlayer;
    this.reason = reason;
    this.bestPart = bestPart;
    this.emotion = emotion;
    this.frequentSituation = frequentSituation;
  }
}
