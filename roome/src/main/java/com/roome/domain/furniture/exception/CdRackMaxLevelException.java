package com.roome.domain.furniture.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdRackMaxLevelException extends BusinessException {

  public CdRackMaxLevelException() {
    super(ErrorCode.CD_RACK_MAX_LEVEL);
  }
}
