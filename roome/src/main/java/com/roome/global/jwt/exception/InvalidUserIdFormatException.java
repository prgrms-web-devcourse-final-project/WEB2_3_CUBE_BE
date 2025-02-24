package com.roome.global.jwt.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class InvalidUserIdFormatException extends BusinessException {
    public InvalidUserIdFormatException() {
        super(ErrorCode.INVALID_USER_ID_FORMAT);
    }
}
