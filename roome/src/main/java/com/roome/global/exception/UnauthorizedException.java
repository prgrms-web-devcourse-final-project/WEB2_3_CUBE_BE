package com.roome.global.exception;

public class UnauthorizedException extends ControllerException {
  public UnauthorizedException() {
    super(ErrorCode.UNAUTHORIZED_ACCESS);
  }
}
