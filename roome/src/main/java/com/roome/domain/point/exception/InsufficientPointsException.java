package com.roome.domain.point.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class InsufficientPointsException extends BusinessException {
  public InsufficientPointsException() {
    super(ErrorCode.INSUFFICIENT_POINTS);
  }
}
