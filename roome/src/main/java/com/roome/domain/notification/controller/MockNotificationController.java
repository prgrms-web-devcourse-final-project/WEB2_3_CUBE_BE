package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mock/notifications")
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
    @PatchMapping("/{notificationId}/read")
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
