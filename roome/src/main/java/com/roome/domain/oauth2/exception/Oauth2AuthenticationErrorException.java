package com.roome.domain.oauth2.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class Oauth2AuthenticationErrorException extends BusinessException {
    public Oauth2AuthenticationErrorException() {
        super(ErrorCode.OAUTH2_AUTHENTICATION_ERROR);
    }
}
