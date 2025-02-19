package com.roome.domain.mybook.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyBookNotFoundException extends BusinessException {

    public MyBookNotFoundException() {
        super(ErrorCode.MY_BOOK_NOT_FOUND);
    }
}
