package com.roome.domain.mycd.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyCdAlreadyExistsException extends BusinessException {

  public MyCdAlreadyExistsException() {
    super(ErrorCode.MYCD_ALREADY_EXISTS);
  }
}
