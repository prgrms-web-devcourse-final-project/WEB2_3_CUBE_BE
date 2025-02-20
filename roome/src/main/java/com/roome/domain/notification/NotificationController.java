package com.roome.domain.notification;

import com.roome.domain.notification.dto.NotificationInfo;
import com.roome.domain.notification.dto.NotificationReadResponse;
import com.roome.domain.notification.dto.NotificationResponse;
import com.roome.domain.notification.dto.NotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    // Mock 데이터 생성
    List<NotificationInfo> mockNotifications = Arrays
            .asList(NotificationInfo
                            .builder()
                            .notificationId(1L)
                            .type(NotificationType.GUESTBOOK)
                            .title("새로운 방명록")
                            .content("방명록이 작성되었습니다.")
                            .senderId("user123")
                            .senderName("John Doe")
                            .senderProfileImage("https://example.com/profile1.jpg")
                            .targetId("guestbook123")
                            .isRead(false)
                            .createdAt("2024-02-14T12:00:00Z")
                            .build(),
            NotificationInfo
                    .builder()
            .notificationId(2L)
            .type(NotificationType.MUSIC_COMMENT)
            .title("새로운 댓글")
            .content("음악에 새로운 댓글이 달렸습니다.")
            .senderId("user124")
            .senderName("Jane Smith")
            .senderProfileImage("https://example.com/profile2.jpg")
            .targetId("music_comment123")
            .isRead(true)
            .createdAt("2024-02-14T11:00:00Z")
            .build());

    @GetMapping
    public ResponseEntity<NotificationResponse> getNotifications(@RequestParam(required = false) String cursor, @RequestParam(required = false, defaultValue = "10") int limit, @RequestParam(required = false) String isRead) {
        List<NotificationInfo> filteredNotifications = mockNotifications
                .stream()
                .filter(notification -> notification.isRead() == true)
                .collect(Collectors.toList());

        NotificationResponse response = NotificationResponse
                .builder()
                .notifications(filteredNotifications)
                .nextCursor("notif456")
                .hasNext(true)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationReadResponse> readNotification(@PathVariable String notificationId) {
        NotificationReadResponse response = NotificationReadResponse
                .builder()
                .type(NotificationType.GUESTBOOK)
                .targetId(1L)
                .senderId(2L)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity readAllNotifications() {
        // Mock: 모든 알림을 읽음 처리했다고 가정
        mockNotifications.forEach(notification -> notification.setRead(true));


        return ResponseEntity.ok().build();
    }
}
