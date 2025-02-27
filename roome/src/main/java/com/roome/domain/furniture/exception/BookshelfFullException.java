package com.roome.domain.furniture.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class BookshelfFullException extends BusinessException {

    public BookshelfFullException() {
        super(ErrorCode.BOOKSHELF_FULL);
    }
}
