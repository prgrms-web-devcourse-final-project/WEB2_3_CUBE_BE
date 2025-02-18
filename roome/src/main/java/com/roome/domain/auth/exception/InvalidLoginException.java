package com.roome.domain.auth.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class InvalidLoginException extends BusinessException {
    public InvalidLoginException() {
        super(ErrorCode.INVALID_LOGIN);
    }
}
