package com.roome.domain.event.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class EventNotStartedException extends BusinessException {

  public EventNotStartedException() {
    super(ErrorCode.EVENT_NOT_STARTED);
  }
}
