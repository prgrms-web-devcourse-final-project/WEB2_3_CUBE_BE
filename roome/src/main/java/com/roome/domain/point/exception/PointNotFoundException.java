package com.roome.domain.point.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class PointNotFoundException extends BusinessException {

  public PointNotFoundException() {
    super(ErrorCode.POINT_NOT_FOUND);
  }
}
