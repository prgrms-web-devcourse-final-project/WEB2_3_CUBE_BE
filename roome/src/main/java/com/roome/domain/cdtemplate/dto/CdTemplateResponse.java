package com.roome.domain.cdtemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CdTemplateResponse {
  private Long templateId;
  private Long myCdId;
  private String reason;
  private String bestPart;
  private String emotion;
  private String frequentSituation;
  private LocalDateTime createdAt;
}
