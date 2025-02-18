package com.roome.global.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 비즈니스 관련 예러 처리
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(BusinessException e) {
    ErrorCode error = e.getErrorCode();
    return ResponseEntity
        .status(error.getStatus())
        .body(new ErrorResponse(error.getMessage(), error.getStatus().value()));
  }

  // DB 관련 예외 처리
  @ExceptionHandler(DataAccessException.class)
  protected ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("데이터베이스 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }
}
