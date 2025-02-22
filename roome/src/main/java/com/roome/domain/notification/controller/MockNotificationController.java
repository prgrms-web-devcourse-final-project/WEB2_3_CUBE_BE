package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mock/notifications")
@Tag(name = "Mock Notification", description = "알림 관련 Mock API - 개발 테스트용")
public class MockNotificationController {

    // Mock 데이터 생성
    // Mock 데이터 생성
    List<NotificationInfo> mockNotifications = Arrays.asList(NotificationInfo
                                                                     .builder()
                                                                     .notificationId(1L)
                                                                     .type(NotificationType.GUESTBOOK)
                                                                     .senderId(1L)
                                                                     .senderNickName("John Doe")
                                                                     .senderProfileImage(
                                                                             "https://example.com/profile1.jpg")
                                                                     .targetId(2L)
                                                                     .createdAt(LocalDateTime.now())
                                                                     .build(), NotificationInfo
                                                                     .builder()
                                                                     .notificationId(2L)
                                                                     .type(NotificationType.MUSIC_COMMENT)
                                                                     .senderId(2L)
                                                                     .senderNickName("Jane Smith")
                                                                     .senderProfileImage(
                                                                             "https://example.com/profile2.jpg")
                                                                     .targetId(1L)
                                                                     .createdAt(LocalDateTime.now())
                                                                     .build(), NotificationInfo
                                                                     .builder()
                                                                     .notificationId(3L)
                                                                     .type(NotificationType.EVENT)
                                                                     .senderId(3L)
                                                                     .senderNickName("Event Team")
                                                                     .senderProfileImage(
                                                                             "https://example.com/profile3.jpg")
                                                                     .targetId(4L)
                                                                     .createdAt(LocalDateTime.now())
                                                                     .build(), NotificationInfo
                                                                     .builder()
                                                                     .notificationId(4L)
                                                                     .type(NotificationType.GUESTBOOK)
                                                                     .senderId(4L)
                                                                     .senderNickName("Sarah Kim")
                                                                     .senderProfileImage(
                                                                             "https://example.com/profile4.jpg")
                                                                     .targetId(3L)
                                                                     .createdAt(LocalDateTime.now())
                                                                     .build(), NotificationInfo
                                                                     .builder()
                                                                     .notificationId(5L)
                                                                     .type(NotificationType.MUSIC_COMMENT)
                                                                     .senderId(5L)
                                                                     .senderNickName("Mike Park")
                                                                     .senderProfileImage(
                                                                             "https://example.com/profile5.jpg")
                                                                     .targetId(4L)
                                                                     .createdAt(LocalDateTime.now())
                                                                     .build());

    @Operation(summary = "Mock 알림 목록 조회", description = "테스트용 Mock 알림 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mock 알림 목록 조회 성공")})
    @GetMapping
    public ResponseEntity<NotificationResponse> getNotifications(@RequestParam(required = false) Long cursor, @RequestParam(required = false, defaultValue = "10") int limit, @RequestParam(required = false, defaultValue = "true") boolean read) {

        // 항상 동일한 응답 반환
        if (read) {
            return ResponseEntity.ok(NotificationResponse
                                             .builder()
                                             .notifications(Arrays.asList(mockNotifications.get(1),
                                                                          mockNotifications.get(3)))  // 읽은 알림만
                                             .nextCursor("")
                                             .hasNext(false)
                                             .build());
        } else {
            return ResponseEntity.ok(NotificationResponse
                                             .builder()
                                             .notifications(
                                                     Arrays.asList(mockNotifications.get(0), mockNotifications.get(2),
                                                                   mockNotifications.get(4)))  // 안 읽은 알림만
                                             .nextCursor("")
                                             .hasNext(false)
                                             .build());
        }
    }

    //알림 읽음
    @Operation(summary = "Mock 알림 읽음 처리", description = "테스트용 Mock 알림을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mock 알림 읽음 처리 성공")})
    public ResponseEntity<NotificationReadResponse> readNotification(@PathVariable Long notificationId) {
        // 고정된 응답 반환
        return ResponseEntity.ok(NotificationReadResponse
                                         .builder()
                                         .type(NotificationType.GUESTBOOK)
                                         .targetId(1L)
                                         .senderId(2L)
                                         .build());
    }

    //알림 생성
    @Operation(summary = "Mock 알림 생성", description = "테스트용 Mock 알림을 생성합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Mock 알림 생성 성공")})
    @PostMapping
    @ResponseBody
    public ResponseEntity<CreateNotificationResponse> createNotification(@RequestBody CreateNotificationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateNotificationResponse
                              .builder()
                              .notificationId(6L)
                              .build());
    }
}
