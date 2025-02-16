package com.roome.global.jwt.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class InvalidJwtTokenException extends BusinessException {
    public InvalidJwtTokenException() {
        super(ErrorCode.INVALID_JWT_TOKEN);
    }
}
