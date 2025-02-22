package com.roome.domain.mybook.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MyBookDuplicateException extends BusinessException {

    public MyBookDuplicateException() {
        super(ErrorCode.MY_BOOK_DUPLICATE);
    }
}
