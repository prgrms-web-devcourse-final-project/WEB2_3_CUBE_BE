package com.roome.domain.mycd.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdRackCapacityExceededException extends BusinessException {

  public CdRackCapacityExceededException() {
    super(ErrorCode.CD_RACK_CAPACITY_EXCEEDED);
  }
}
