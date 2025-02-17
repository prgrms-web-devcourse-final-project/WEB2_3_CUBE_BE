package com.roome.domain.mybookreview.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyBookReviewNotFoundException extends BusinessException {

    public MyBookReviewNotFoundException() {
        super(ErrorCode.MY_BOOK_REVIEW_NOT_FOUND);
    }
}
