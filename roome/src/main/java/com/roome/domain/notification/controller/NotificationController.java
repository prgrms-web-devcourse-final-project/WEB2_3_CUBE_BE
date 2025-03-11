package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.NotificationReadResponse;
import com.roome.domain.notification.dto.NotificationResponse;
import com.roome.domain.notification.dto.NotificationSearchCondition;
import com.roome.domain.notification.service.NotificationService;
import com.roome.global.auth.AuthenticatedUser;
import com.roome.global.exception.ControllerException;
import com.roome.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "알림 API", description = "사용자의 알림 조회 및 읽음 처리 기능을 제공합니다.")
public class NotificationController {

  private final NotificationService notificationService;

  // 알림 조회 API
  @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이지네이션으로 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 limit 값 (INVALID_LIMIT_VALUE)"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 cursor 값 (INVALID_CURSOR_VALUE)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @GetMapping
  public ResponseEntity<NotificationResponse> getNotifications(
      @Parameter(description = "페이지네이션 커서 (마지막으로 받은 알림 ID)", example = "10")
      @RequestParam(required = false) Long cursor,

      @Parameter(description = "한 페이지당 조회할 알림 수(1-100)", example = "10")
      @RequestParam(required = false, defaultValue = "10") int limit,

      @Parameter(description = "읽음 상태로 필터링 (true: 읽은 알림, false: 읽지 않은 알림, null: 모든 알림)", example = "false")
      @RequestParam(required = false) Boolean read,

      @AuthenticatedUser Long userId) {

    // 커서 검증
    if (cursor != null && cursor <= 0) {
      log.error("유효하지 않은 cursor 값: {}", cursor);
      throw new ControllerException(ErrorCode.INVALID_CURSOR_VALUE);
    }

    // limit 직접 검증
    if (limit < 1 || limit > 100) {
      log.error("유효하지 않은 limit 값: {}", limit);
      throw new ControllerException(ErrorCode.INVALID_LIMIT_VALUE);
    }

    NotificationSearchCondition condition = NotificationSearchCondition
        .builder()
        .cursor(cursor)
        .limit(limit)
        .read(read)
        .receiverId(userId)
        .build();

    return ResponseEntity.ok(notificationService.getNotifications(condition));
  }

  // 알림 읽음 API
  @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
      @ApiResponse(responseCode = "400", description = "이미 읽음 처리된 알림 (NOTIFICATION_ALREADY_READ)"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "알림에 대한 접근 권한 없음 (NOTIFICATION_ACCESS_DENIED)"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 (NOTIFICATION_NOT_FOUND)")
  })
  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<NotificationReadResponse> readNotification(
      @Parameter(description = "읽음 처리할 알림의 ID", example = "1")
      @PathVariable Long notificationId,

      @AuthenticatedUser Long userId) {

    // 알림 ID 검증
    if (notificationId <= 0) {
      throw new ControllerException(ErrorCode.INVALID_CURSOR_VALUE);
    }
    NotificationReadResponse response = notificationService.readNotification(notificationId,
        userId);
    return ResponseEntity.ok(response);
  }
}