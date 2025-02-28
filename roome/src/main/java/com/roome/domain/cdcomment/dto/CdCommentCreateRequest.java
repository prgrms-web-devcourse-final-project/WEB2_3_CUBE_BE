package com.roome.domain.cdcomment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CdCommentCreateRequest {

  @NotBlank
  private String timestamp;

  @NotBlank
  private String content;
}

