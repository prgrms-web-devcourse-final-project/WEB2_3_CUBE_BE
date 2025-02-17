package com.roome.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // OAuth2 로그인 관련 예외
  UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 로그인 방식입니다."),
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
  OAUTH2_AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "소셜 로그인 인증 중 오류가 발생했습니다."),
  INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
  DISABLED_ACCOUNT(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),

  // JWT 관련 예외
  INVALID_JWT_TOKEN(HttpStatus.BAD_REQUEST, "JWT 토큰이 유효하지 않거나, 입력값이 비어 있습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh 토큰이 유효하지 않거나, 입력값이 비어 있습니다."),
  MISSING_AUTHORITY(HttpStatus.UNAUTHORIZED, "해당 토큰에는 권한 정보가 포함되어 있지 않습니다."),

  // User 관련 예외
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

  // Room 관련 예외
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방을 찾을 수 없습니다.")

  ;

  private final HttpStatus status;
  private final String message;
}
