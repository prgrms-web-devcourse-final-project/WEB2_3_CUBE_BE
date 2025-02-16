package com.roome.domain.oauth2.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class AuthenticationFailedException extends BusinessException {
    public AuthenticationFailedException() {
        super(ErrorCode.AUTHENTICATION_FAILED);
    }
}