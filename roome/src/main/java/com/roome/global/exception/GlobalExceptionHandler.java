package com.roome.global.exception;

import com.roome.domain.auth.dto.response.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 비즈니스 관련 예외 처리
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(BusinessException e) {
    ErrorCode error = e.getErrorCode();
    return ResponseEntity
        .status(error.getStatus())
        .body(new ErrorResponse(error.getMessage(), error.getStatus().value()));
  }

  @ExceptionHandler(ControllerException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(ControllerException e) {
    ErrorCode error = e.getErrorCode();
    return ResponseEntity
        .status(error.getStatus())
        .body(new ErrorResponse(error.getMessage(), error.getStatus().value()));
  }

  // ForbiddenException 처리 추가 (403 반환)
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN.value()));
  }

  // DB 관련 예외 처리
  @ExceptionHandler(DataAccessException.class)
  protected ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("데이터베이스 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  // Authorization 헤더가 없는 경우 401 반환
  @ExceptionHandler(MissingRequestHeaderException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public MessageResponse handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
    return new MessageResponse("인증 토큰이 필요합니다.");
  }

  // 기타 모든 예외는 500으로 처리 (ForbiddenException보다 아래에 있어야 함)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e, WebRequest request) {
    log.error("처리되지 않은 예외 발생: {}", e.getMessage(), e);

    ErrorResponse response = new ErrorResponse(
        "요청을 처리하는 중 오류가 발생했습니다: " + e.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(response);
  }
}
