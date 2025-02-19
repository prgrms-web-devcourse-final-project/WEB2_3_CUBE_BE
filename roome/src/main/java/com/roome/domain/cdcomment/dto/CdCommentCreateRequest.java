package com.roome.domain.cdcomment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CdCommentCreateRequest {
  private Long timestampSec;  // 타임스탬프 (선택 사항, 없으면 null)
  private String comment;  // 댓글 내용
}
