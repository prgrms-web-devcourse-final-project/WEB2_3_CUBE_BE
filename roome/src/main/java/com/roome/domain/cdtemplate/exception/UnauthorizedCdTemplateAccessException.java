package com.roome.domain.cdtemplate.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class UnauthorizedCdTemplateAccessException extends BusinessException {

  public UnauthorizedCdTemplateAccessException() {
    super(ErrorCode.UNAUTHORIZED_CD_TEMPLATE_ACCESS);
  }
}
