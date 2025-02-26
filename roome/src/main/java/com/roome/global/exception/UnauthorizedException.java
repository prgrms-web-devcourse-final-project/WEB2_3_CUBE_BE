package com.roome.global.exception;

public class UnauthorizedException extends BusinessException {

  public UnauthorizedException() {
    super(ErrorCode.UNAUTHORIZED_ACCESS);
  }
}
