package com.roome.domain.point.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class InvalidPointOperationException extends BusinessException {
  public InvalidPointOperationException() {
    super(ErrorCode.INVALID_POINT_OPERATION);
  }
}
