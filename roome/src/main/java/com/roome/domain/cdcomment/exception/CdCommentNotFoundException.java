package com.roome.domain.cdcomment.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdCommentNotFoundException extends BusinessException {

  public CdCommentNotFoundException() {
    super(ErrorCode.CD_COMMENT_NOT_FOUND);
  }
}
