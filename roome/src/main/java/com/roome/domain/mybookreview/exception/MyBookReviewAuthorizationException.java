package com.roome.domain.mybookreview.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyBookReviewAuthorizationException extends BusinessException {

    public MyBookReviewAuthorizationException() {
        super(ErrorCode.MY_BOOK_REVIEW_ACCESS_DENIED);
    }
}
