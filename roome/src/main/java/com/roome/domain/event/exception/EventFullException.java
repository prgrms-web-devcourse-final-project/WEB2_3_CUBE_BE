package com.roome.domain.event.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class EventFullException extends BusinessException {

  public EventFullException() {
    super(ErrorCode.EVENT_FULL);
  }
}
