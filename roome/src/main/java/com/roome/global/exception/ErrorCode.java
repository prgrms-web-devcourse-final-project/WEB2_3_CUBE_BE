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
  INVALID_BIO_LENGTH(HttpStatus.BAD_REQUEST, "자기소개는 100자를 초과할 수 없습니다."),
  INVALID_BIO_NULL(HttpStatus.BAD_REQUEST, "자기소개는 비어 있을 수 없습니다."),

  // Auth 관련 예외
  UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

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
  BOOKSHELF_MAX_LEVEL(HttpStatus.BAD_REQUEST, "책장 레벨을 더 이상 올릴 수 없습니다. 책장 레벨이 최대입니다."),
  BOOKSHELF_UPGRADE_DENIED(HttpStatus.BAD_REQUEST, "책장 레벨을 업그레이드할 수 없습니다. 이전 레벨을 먼저 업그레이드 해야합니다."),

  // 방명록 관련 예외
  GUESTBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 방명록을 찾을 수 없습니다."),
  GUESTBOOK_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "방의 주인이거나 방명록 작성자가 아닙니다."),

  // 결제 관련 예외
  PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제 정보를 찾을 수 없습니다."),
  PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
  PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "결제 검증에 실패했습니다."),
  PAYMENT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다."),
  PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 결제에 대한 접근 권한이 없습니다."),
  PAYMENT_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "이 결제는 취소할 수 없습니다."),
  PAYMENT_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 취소 중 오류가 발생했습니다."),


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
  MYCD_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 MyCd를 삭제할 권한이 없습니다."),
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
  NOTIFICATION_EVENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 이벤트 처리 중 오류가 발생했습니다."),
  NOTIFICATION_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 생성에 실패했습니다."),

  // Point 관련 예외
  INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
  DUPLICATE_POINT_EARN(HttpStatus.BAD_REQUEST, "해당 포인트는 하루 1회만 적립 가능합니다."),
  POINT_HISTORY_EMPTY(HttpStatus.BAD_REQUEST, "포인트 내역이 존재하지 않습니다."),
  INVALID_POINT_OPERATION(HttpStatus.BAD_REQUEST, "잘못된 포인트 조회 요청입니다."),

  // 이벤트 관련 예외
  EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다."),
  EVENT_NOT_STARTED(HttpStatus.BAD_REQUEST, "이벤트가 아직 시작되지 않았습니다."),
  EVENT_FULL(HttpStatus.BAD_REQUEST, "이벤트의 최대 참여 인원이 초과되었습니다."),
  ALREADY_PARTICIPATED(HttpStatus.BAD_REQUEST, "이미 참여한 이벤트입니다."),

  // 이미지 업로드 관련 예외
  POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 정보를 찾을 수 없습니다."),
  IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "이미지 파일이 없거나 비어 있습니다."),
  INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "지원되지 않는 이미지 형식입니다."),
  IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 크기가 제한을 초과했습니다."),
  S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 오류가 발생했습니다."),
  INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "잘못된 이미지 URL입니다."),
  S3_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제 중 오류가 발생했습니다."),

  // WebSocket 관련 예외
  WEBSOCKET_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "웹소켓 연결에 필요한 인증 토큰이 없습니다."),
  WEBSOCKET_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "웹소켓 연결에 사용된 토큰이 유효하지 않습니다."),
  WEBSOCKET_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "웹소켓 연결에 사용된 토큰이 블랙리스트에 등록되어 있습니다."),
  NOTIFICATION_DELIVERY_FAILED(HttpStatus.SERVICE_UNAVAILABLE,
      "알림 메시지 전송에 실패했습니다. 수신자가 연결되어 있지 않거나 대상을 찾을 수 없습니다."),
  NOTIFICATION_BROKER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알림 메시지 브로커 오류가 발생했습니다."),
  NOTIFICATION_SENDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알림 메시지 전송 중 오류가 발생했습니다."),

  //알림 파라미터 관련 예외
  NOTIFICATION_INVALID_RECEIVER(HttpStatus.BAD_REQUEST, "알림 수신자 ID가 유효하지 않습니다."),
  NOTIFICATION_INVALID_TYPE(HttpStatus.BAD_REQUEST, "알림 타입이 유효하지 않습니다."),
  NOTIFICATION_INVALID_ID(HttpStatus.BAD_REQUEST, "알림 ID가 유효하지 않습니다."),

  // 서버 에러
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다.");

  private final HttpStatus status;
  private final String message;
}
