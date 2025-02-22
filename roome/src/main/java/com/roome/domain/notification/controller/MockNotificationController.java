package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.NotificationInfo;
import com.roome.domain.notification.dto.NotificationReadResponse;
import com.roome.domain.notification.dto.NotificationResponse;
import com.roome.domain.notification.dto.NotificationType;
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
    List<NotificationInfo> mockNotifications = Arrays.asList(
            NotificationInfo.builder()
                            .notificationId(1L)
                            .type(NotificationType.GUESTBOOK)
                            .title("새로운 방명록")
                            .content("방명록이 작성되었습니다.")
                            .senderId(1L)
                            .senderName("John Doe")
                            .senderProfileImage("https://example.com/profile1.jpg")
                            .targetId(2L)
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build(),
            NotificationInfo.builder()
                            .notificationId(2L)
                            .type(NotificationType.MUSIC_COMMENT)
                            .title("새로운 댓글")
                            .content("음악에 새로운 댓글이 달렸습니다.")
                            .senderId(2L)
                            .senderName("Jane Smith")
                            .senderProfileImage("https://example.com/profile2.jpg")
                            .targetId(1L)
                            .isRead(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
            NotificationInfo.builder()
                            .notificationId(3L)
                            .type(NotificationType.EVENT)
                            .title("새로운 이벤트")
                            .content("새로운 이벤트가 등록되었습니다.")
                            .senderId(3L)
                            .senderName("Event Team")
                            .senderProfileImage("https://example.com/profile3.jpg")
                            .targetId(4L)
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build(),
            NotificationInfo.builder()
                            .notificationId(4L)
                            .type(NotificationType.GUESTBOOK)
                            .title("새로운 방명록")
                            .content("방명록이 작성되었습니다.")
                            .senderId(4L)
                            .senderName("Sarah Kim")
                            .senderProfileImage("https://example.com/profile4.jpg")
                            .targetId(3L)
                            .isRead(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
            NotificationInfo.builder()
                            .notificationId(5L)
                            .type(NotificationType.MUSIC_COMMENT)
                            .title("새로운 댓글")
                            .content("음악에 새로운 댓글이 달렸습니다.")
                            .senderId(5L)
                            .senderName("Mike Park")
                            .senderProfileImage("https://example.com/profile5.jpg")
                            .targetId(4L)
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build()
                                                            );

    @GetMapping
    public ResponseEntity<NotificationResponse> getNotifications(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "true") boolean isRead) {

        // 항상 동일한 응답 반환
        if (isRead) {
            return ResponseEntity.ok(NotificationResponse.builder()
                                                         .notifications(Arrays.asList(mockNotifications.get(1), mockNotifications.get(3)))  // 읽은 알림만
                                                         .nextCursor("")
                                                         .hasNext(false)
                                                         .build());
        } else {
            return ResponseEntity.ok(NotificationResponse.builder()
                                                         .notifications(Arrays.asList(mockNotifications.get(0), mockNotifications.get(2), mockNotifications.get(4)))  // 안 읽은 알림만
                                                         .nextCursor("")
                                                         .hasNext(false)
                                                         .build());
        }
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationReadResponse> readNotification(@PathVariable Long notificationId) {
        // 고정된 응답 반환
        return ResponseEntity.ok(NotificationReadResponse.builder()
                                                         .type(NotificationType.GUESTBOOK)
                                                         .targetId(1L)
                                                         .senderId(2L)
                                                         .build());
    }

}
