package com.roome.domain.auth.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class InvalidProviderException extends BusinessException {
    public InvalidProviderException() {
        super(ErrorCode.INVALID_PROVIDER);
    }
}
