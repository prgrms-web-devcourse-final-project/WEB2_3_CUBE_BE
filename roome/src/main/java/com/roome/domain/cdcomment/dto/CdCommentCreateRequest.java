package com.roome.domain.cdcomment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CdCommentCreateRequest {

  @NotNull
  private int timestamp;

  @NotBlank
  private String content;
}

