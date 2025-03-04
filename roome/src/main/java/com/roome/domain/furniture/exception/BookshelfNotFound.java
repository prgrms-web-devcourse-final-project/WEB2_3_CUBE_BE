package com.roome.domain.furniture.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class BookshelfNotFound extends BusinessException {

    public BookshelfNotFound() {
        super(ErrorCode.BOOKSHELF_NOT_FOUND);
    }
}
