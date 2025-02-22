package com.roome.domain.cdtemplate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CdTemplateRequest {

  @NotBlank
  private String comment1;

  @NotBlank
  private String comment2;

  @NotBlank
  private String comment3;

  @NotBlank
  private String comment4;
}
