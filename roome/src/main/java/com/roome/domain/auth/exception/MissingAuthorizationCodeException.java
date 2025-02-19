package com.roome.domain.auth.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingAuthorizationCodeException extends BusinessException {
    public MissingAuthorizationCodeException() {
        super(ErrorCode.MISSING_AUTHORIZATION_CODE);
    }
}