package com.roome.domain.oauth2.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class UnsupportedOAuth2ProviderException extends BusinessException {
    public UnsupportedOAuth2ProviderException() {
        super(ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
    }
}
