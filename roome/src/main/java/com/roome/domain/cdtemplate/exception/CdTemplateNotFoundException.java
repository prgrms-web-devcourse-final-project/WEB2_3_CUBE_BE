package com.roome.domain.cdtemplate.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdTemplateNotFoundException extends BusinessException {

  public CdTemplateNotFoundException() {
    super(ErrorCode.CD_TEMPLATE_NOT_FOUND);
  }
}
