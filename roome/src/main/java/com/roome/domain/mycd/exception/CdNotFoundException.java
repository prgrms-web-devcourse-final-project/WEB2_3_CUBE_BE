package com.roome.domain.mycd.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdNotFoundException extends BusinessException {

  public CdNotFoundException(Long cdId) {
    super(ErrorCode.CD_NOT_FOUND);
  }
}
