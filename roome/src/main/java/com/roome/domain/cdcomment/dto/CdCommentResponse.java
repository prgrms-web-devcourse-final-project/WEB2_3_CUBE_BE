package com.roome.domain.cdcomment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CdCommentResponse {
  private Long commentId;  // 댓글 ID
  private Long myCdId;  // CD ID
  private Long authorId;  // 작성자 ID
  private String nickname;  // 작성자 닉네임
  private Long timestampSec;  // 타임스탬프 (초) (선택 사항)
  private String content;  // 댓글 내용
  private LocalDateTime createdAt;  // 작성 날짜
}
