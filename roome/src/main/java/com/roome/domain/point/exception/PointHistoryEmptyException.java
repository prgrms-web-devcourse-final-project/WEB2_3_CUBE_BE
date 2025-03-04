package com.roome.domain.point.exception;

import com.roome.global.exception.ErrorCode;
import com.roome.global.exception.BusinessException;

public class PointHistoryEmptyException extends BusinessException {
  public PointHistoryEmptyException() {
    super(ErrorCode.POINT_HISTORY_EMPTY);
  }
}
