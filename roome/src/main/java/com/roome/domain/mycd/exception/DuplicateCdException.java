package com.roome.domain.mycd.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class DuplicateCdException extends BusinessException {

  public DuplicateCdException(Long cdId) {
    super(ErrorCode.DUPLICATE_CD);
  }
}
