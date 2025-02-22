package com.roome.domain.cdcomment.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdCommentListEmptyException extends BusinessException {

  public CdCommentListEmptyException() {
    super(ErrorCode.CD_COMMENT_LIST_EMPTY);
  }
}
