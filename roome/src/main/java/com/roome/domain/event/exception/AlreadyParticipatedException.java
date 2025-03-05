package com.roome.domain.event.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class AlreadyParticipatedException extends BusinessException {

  public AlreadyParticipatedException() {
    super(ErrorCode.ALREADY_PARTICIPATED);
  }
}
