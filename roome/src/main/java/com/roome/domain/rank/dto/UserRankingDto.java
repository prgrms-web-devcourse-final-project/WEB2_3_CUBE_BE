package com.roome.domain.rank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRankingDto {

  private int rank;
  private Long userId;
  private String nickname;
  private String profileImage;
  private int score;
  private boolean isTopRank; // 1~3위 여부
}