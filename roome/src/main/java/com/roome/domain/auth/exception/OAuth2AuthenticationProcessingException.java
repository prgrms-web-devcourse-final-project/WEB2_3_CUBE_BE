package com.roome.domain.auth.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class OAuth2AuthenticationProcessingException extends BusinessException {
    public OAuth2AuthenticationProcessingException() {
        super(ErrorCode.OAUTH2_AUTHENTICATION_PROCESSING);
    }
}
