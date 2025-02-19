package com.roome.domain.mybookreview.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyBookAuthorizationException extends BusinessException {

    public MyBookAuthorizationException() {
        super(ErrorCode.MY_BOOK_ACCESS_DENIED);
    }
}
