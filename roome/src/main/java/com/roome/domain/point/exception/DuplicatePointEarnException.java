package com.roome.domain.point.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class DuplicatePointEarnException extends BusinessException {
  public DuplicatePointEarnException() {
    super(ErrorCode.DUPLICATE_POINT_EARN);
  }
}
