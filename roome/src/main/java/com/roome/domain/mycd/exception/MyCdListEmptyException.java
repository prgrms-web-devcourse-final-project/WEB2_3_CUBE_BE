package com.roome.domain.mycd.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyCdListEmptyException extends BusinessException {
  public MyCdListEmptyException() {
    super(ErrorCode.MYCD_LIST_EMPTY);
  }
}
