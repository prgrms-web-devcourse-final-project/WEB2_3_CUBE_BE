package com.roome.domain.event.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class EventNotFoundException extends BusinessException {

  public EventNotFoundException() {
    super(ErrorCode.EVENT_NOT_FOUND);
  }
}
