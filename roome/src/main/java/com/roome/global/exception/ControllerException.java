package com.roome.global.exception;

public class ControllerException extends RuntimeException {
    private final ErrorCode errorCode;

    public ControllerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
