package com.roome.domain.cdtemplate.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CdTemplateCreateRequest {
  private String reason;
  private String bestPart;
  private String emotion;
  private String frequentSituation;
}
