package com.roome.domain.furniture.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class BookshelfUpgradeDenyException extends BusinessException {

    public BookshelfUpgradeDenyException() {
        super(ErrorCode.BOOKSHELF_UPGRADE_DENIED);
    }
}
