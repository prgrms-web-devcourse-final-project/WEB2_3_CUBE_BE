package com.roome.domain.furniture.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class BookshelfMaxLevelException extends BusinessException {

    public BookshelfMaxLevelException() {
        super(ErrorCode.BOOKSHELF_MAX_LEVEL);
    }
}
