package com.roome.domain.cdcomment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CdCommentResponse {

  private Long id;
  private Long myCdId;
  private Long userId;
  private String nickname;
  private int timestamp;
  private String content;
  private LocalDateTime createdAt;
}
