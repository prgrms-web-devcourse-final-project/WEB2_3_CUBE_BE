package com.roome.domain.mycd.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyCdNotFoundException extends BusinessException {
  public MyCdNotFoundException() {
    super(ErrorCode.MYCD_NOT_FOUND);
  }
}
