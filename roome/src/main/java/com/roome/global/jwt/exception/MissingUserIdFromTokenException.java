package com.roome.global.jwt.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class MissingUserIdFromTokenException extends BusinessException {
    public MissingUserIdFromTokenException() {
        super(ErrorCode.MISSING_USER_ID_FROM_TOKEN);
    }
}
