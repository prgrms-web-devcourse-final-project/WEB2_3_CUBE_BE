package com.roome.global.exception;

public class LockAcquisitionFailedException extends RuntimeException {
  public LockAcquisitionFailedException() {
    super("락 획득에 실패하였습니다.");
  }
}
