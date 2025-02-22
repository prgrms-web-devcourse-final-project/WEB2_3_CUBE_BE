package com.roome.domain.mybookreview.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyBookReviewDuplicateException extends BusinessException {

    public MyBookReviewDuplicateException() {
        super(ErrorCode.MY_BOOK_REVIEW_DUPLICATE);
    }
}
