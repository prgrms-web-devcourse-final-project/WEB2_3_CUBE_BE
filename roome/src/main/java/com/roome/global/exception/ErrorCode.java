package com.roome.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // OAuth2 로그인 관련 예외
  INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 로그인 방식입니다."),
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
  OAUTH2_AUTHENTICATION_PROCESSING(HttpStatus.UNAUTHORIZED, "소셜 로그인 처리 중 오류가 발생했습니다."),
  INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
  DISABLED_ACCOUNT(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),
  MISSING_AUTHORIZATION_CODE(HttpStatus.BAD_REQUEST, "로그인 요청에는 authorization code가 필요합니다."),

  // JWT 관련 예외
  INVALID_JWT_TOKEN(HttpStatus.BAD_REQUEST, "JWT 토큰이 유효하지 않거나, 입력값이 비어 있습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "Refresh 토큰이 유효하지 않거나, 입력값이 비어 있습니다."),
  MISSING_AUTHORITY(HttpStatus.UNAUTHORIZED, "해당 토큰에는 권한 정보가 포함되어 있지 않습니다."),
  INVALID_USER_ID_FORMAT(HttpStatus.UNAUTHORIZED, "토큰의 userId 형식이 올바르지 않습니다."),
  MISSING_USER_ID_FROM_TOKEN(HttpStatus.UNAUTHORIZED, "토큰에서 userId를 찾을 수 없습니다."),

  // User 관련 예외
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다."),
  INVALID_BIO_LENGTH(HttpStatus.BAD_REQUEST, "자기소개는 30자를 초과할 수 없습니다."),

  // 페이지네이션 관련 예외
  INVALID_LIMIT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 limit 값입니다. (1-100 사이의 값을 입력해주세요)"),
  INVALID_CURSOR_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 cursor 값입니다."),

  // 하우스메이트 관련 예외
  SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 하우스메이트로 추가할 수 없습니다."),
  ALREADY_HOUSEMATE(HttpStatus.BAD_REQUEST, "이미 하우스메이트로 추가된 사용자입니다."),
  NOT_HOUSEMATE(HttpStatus.BAD_REQUEST, "하우스메이트로 추가되지 않은 사용자입니다."),

  // Room 관련 예외
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방을 찾을 수 없습니다."),
  INVALID_ROOM_THEME(HttpStatus.BAD_REQUEST, "해당 테마가 유효하지 않거나 입력 값이 비어 있습니다."),
  ROOM_ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "해당 방의 소유주가 아닙니다."),

  // 가구 관련 예외
  INVALID_FURNITURE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 가구 타입입니다."),
  FURNITURE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방에서 요청한 가구를 찾을 수 없습니다."),
  BOOKSHELF_FULL(HttpStatus.BAD_REQUEST, "책장에 더 이상 책을 추가할 수 없습니다. 책장을 업그레이드 해주세요."),

  // 방명록 관련 예외
  GUESTBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방명록을 찾을 수 없습니다."),
  GUESTBOOK_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "방의 주인이거나 방명록 작성자가 아닙니다."),

  // 서평 관련 예외
  MY_BOOK_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "서평을 찾을 수 없습니다."),
  MY_BOOK_REVIEW_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "서평에 대한 권한이 없는 사용자입니다."),
  MY_BOOK_REVIEW_DUPLICATE(HttpStatus.BAD_REQUEST, "서평이 작성된 도서입니다."),

  // 등록 도서 관련 예외
  MY_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "등록 도서를 찾을 수 없습니다."),
  MY_BOOK_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "등록 도서에 대한 권한이 없는 사용자입니다."),
  MY_BOOK_DUPLICATE(HttpStatus.BAD_REQUEST, "책장에 등록된 도서입니다."),

  // CD 관련 예외
  MYCD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 MyCd를 찾을 수 없습니다."),
  MYCD_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 추가된 CD입니다."),
  MYCD_LIST_EMPTY(HttpStatus.NOT_FOUND, "CD 목록이 비어 있습니다."),
  CD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 CD를 찾을 수 없습니다."),
  DUPLICATE_CD(HttpStatus.CONFLICT, "이미 추가된 CD입니다."),
  CD_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
  CD_COMMENT_LIST_EMPTY(HttpStatus.NOT_FOUND, "댓글 목록이 비어 있습니다."),
  CD_COMMENT_SEARCH_EMPTY(HttpStatus.NOT_FOUND, "검색된 댓글이 없습니다."),
  CD_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 CD 템플릿을 찾을 수 없습니다."),
  CD_COMMENT_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "해당 CD 댓글에 대한 권한이 없습니다."),
  UNAUTHORIZED_CD_TEMPLATE_ACCESS(HttpStatus.BAD_REQUEST, "해당 CD 템플릿에 대한 권한이 없습니다."),

  // 알림 관련 예외
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 데이터가 존재하지 않습니다."),
  NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "이미 읽음 처리된 알림입니다."),
  NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 알림에 접근 권한이 없습니다."),
  NOTIFICATION_EXPIRED(HttpStatus.GONE, "만료된 알림입니다."),
  INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 알림 타입입니다."),
  INVALID_NOTIFICATION_REQUEST(HttpStatus.BAD_REQUEST, "알림 생성 요청이 유효하지 않습니다."),

  // 유저 Genre 관련 예외
  GENRE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "장르는 최대 3개까지만 선택할 수 있습니다."),
  DUPLICATE_GENRE(HttpStatus.BAD_REQUEST, "이미 선택된 장르입니다."),
  INVALID_GENRE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 장르 타입입니다."),

  // Point 관련 예외
  INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),

  // 서버 에러
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다.");

  private final HttpStatus status;
  private final String message;
}
