package com.roome.domain.cdcomment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CdCommentCreateRequest {
  @NotBlank
  private String timestamp;

  @NotBlank
  private String content;
}

