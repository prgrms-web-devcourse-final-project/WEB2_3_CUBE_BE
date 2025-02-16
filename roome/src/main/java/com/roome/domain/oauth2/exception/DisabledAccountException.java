package com.roome.domain.oauth2.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class DisabledAccountException extends BusinessException {
    public DisabledAccountException() {
        super(ErrorCode.DISABLED_ACCOUNT);
    }
}