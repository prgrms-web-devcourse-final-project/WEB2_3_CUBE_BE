package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.*;
import com.roome.domain.notification.service.NotificationService;
import com.roome.domain.user.temp.UserPrincipal;
import com.roome.global.exception.ControllerException;
import com.roome.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 조회 API
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이지네이션으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<NotificationResponse> getNotifications(@RequestParam(required = false) Long cursor, @RequestParam(required = false, defaultValue = "10") int limit, @RequestParam(required = false) Boolean read, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NotificationSearchCondition condition = NotificationSearchCondition
                .builder()
                .cursor(cursor)
                .limit(limit)
                .read(read)
                .receiverId(userPrincipal.getId())
                .build();

        return ResponseEntity.ok(notificationService.getNotifications(condition));
    }

    // 알림 읽음 API
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationReadResponse> readNotification(@PathVariable Long notificationId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.readNotification(notificationId, userPrincipal.getId()));
    }

    // 알림 생성 API
    @Operation(summary = "알림 생성", description = "새로운 알림을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "알림 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    public ResponseEntity<CreateNotificationResponse> createNotification(@RequestBody @Valid CreateNotificationRequest request) {
        validateCreateNotificationRequest(request); // 알림 생성 요청 검증
        Long notificationId = notificationService.createNotification(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateNotificationResponse
                              .builder()
                              .notificationId(notificationId)
                              .build());
    }

    // 알림 생성 요청 검증
    private void validateCreateNotificationRequest(CreateNotificationRequest request) {
        if (request.getSenderId() == null || request.getReceiverId() == null
                || request.getTargetId() == null || request.getType() == null) {
            throw new ControllerException(ErrorCode.INVALID_NOTIFICATION_REQUEST);
        }
    }
}