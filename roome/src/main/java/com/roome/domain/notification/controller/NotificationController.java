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


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 조회 API
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
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationReadResponse> readNotification(@PathVariable Long notificationId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.readNotification(notificationId, userPrincipal.getId()));
    }

    // 알림 생성 API
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