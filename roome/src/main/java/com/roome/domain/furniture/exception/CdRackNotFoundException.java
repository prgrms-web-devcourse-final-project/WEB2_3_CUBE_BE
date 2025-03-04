package com.roome.domain.furniture.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdRackNotFoundException extends BusinessException {

  public CdRackNotFoundException() {
    super(ErrorCode.CD_RACK_NOT_FOUND);
  }
}

