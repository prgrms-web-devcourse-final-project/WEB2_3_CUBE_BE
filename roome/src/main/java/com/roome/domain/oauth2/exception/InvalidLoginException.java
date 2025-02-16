package com.roome.domain.oauth2.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class InvalidLoginException extends BusinessException {
    public InvalidLoginException() {
        super(ErrorCode.INVALID_LOGIN);
    }
}
