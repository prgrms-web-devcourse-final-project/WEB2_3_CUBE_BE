package com.roome.domain.mycd.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyCdUnauthorizedException extends BusinessException {

  public MyCdUnauthorizedException() {
    super(ErrorCode.MYCD_DELETE_FORBIDDEN);
  }
}
