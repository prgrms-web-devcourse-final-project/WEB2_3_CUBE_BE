package com.roome.domain.cdcomment.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class CdCommentSearchEmptyException extends BusinessException {

  public CdCommentSearchEmptyException() {
    super(ErrorCode.CD_COMMENT_SEARCH_EMPTY);
  }
}
