package com.roome.domain.cdtemplate.dto;

import com.roome.domain.cdtemplate.entity.CdTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CdTemplateResponse {
  private Long id;
  private Long myCdId;
  private String comment1;
  private String comment2;
  private String comment3;
  private String comment4;

  public static CdTemplateResponse from(CdTemplate cdTemplate) {
    return new CdTemplateResponse(
        cdTemplate.getId(),
        cdTemplate.getMyCd().getId(),
        cdTemplate.getComment1(),
        cdTemplate.getComment2(),
        cdTemplate.getComment3(),
        cdTemplate.getComment4()
    );
  }
}
