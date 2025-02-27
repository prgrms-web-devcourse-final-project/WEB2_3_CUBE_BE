package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.NotificationInfo;
import com.roome.domain.notification.dto.NotificationReadResponse;
import com.roome.domain.notification.dto.NotificationResponse;
import com.roome.domain.notification.dto.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mock/notifications")
@Tag(name = "Mock Notification", description = "알림 관련 Mock API - 개발 테스트용")
public class MockNotificationController {

    // Mock 데이터 생성 - 더 많은 데이터 추가
    List<NotificationInfo> mockNotifications = generateMockNotifications();

    private List<NotificationInfo> generateMockNotifications() {
        List<NotificationInfo> notifications = new ArrayList<>();

        // 알림 타입 배열
        NotificationType[] types = NotificationType.values();

        for (int i = 1; i <= 100; i++) {
            // i에 따라 읽음 상태 결정 (짝수는 읽음, 홀수는 안읽음)
            boolean isRead = i % 2 == 0;

            // 시간은 최신순으로 생성 (id가 클수록 최신)
            LocalDateTime createdTime = LocalDateTime.now().minusHours(100 - i);

            // 알림 타입은 순환하면서 배정
            NotificationType type = types[i % types.length];

            notifications.add(
                    NotificationInfo.builder()
                            .notificationId((long) i)
                            .type(type)
                            .senderId((long) (i % 10 + 1))
                            .senderNickName("User " + (i % 10 + 1))
                            .senderProfileImage("https://example.com/profile" + (i % 10 + 1) + ".jpg")
                            .targetId((long) (i % 5 + 1))
                            .createdAt(createdTime)
                            .isRead(isRead)
                            .build()
            );
        }

        // ID 내림차순으로 정렬 (최신순)
        return notifications.stream()
                .sorted(Comparator.comparing(NotificationInfo::getNotificationId).reversed())
                .collect(Collectors.toList());
    }

    @Operation(summary = "Mock 알림 목록 조회", description = "테스트용 Mock 알림 목록을 무한 스크롤 방식으로 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mock 알림 목록 조회 성공")})
    @GetMapping
    public ResponseEntity<NotificationResponse> getNotifications(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false) Boolean read) {

        // 필터링을 위한 임시 리스트 (이미 ID 내림차순으로 정렬됨)
        List<NotificationInfo> filteredList = new ArrayList<>(mockNotifications);

        // 읽음 상태로 필터링
        if (read != null) {
            filteredList = filteredList.stream()
                    .filter(notification -> notification.getIsRead() == read)
                    .collect(Collectors.toList());
        }

        // 커서 기반 페이징 처리
        int startIndex = 0;
        if (cursor != null) {
            // 커서보다 작은 ID의 알림을 찾아 시작 인덱스 결정
            for (int i = 0; i < filteredList.size(); i++) {
                if (filteredList.get(i).getNotificationId() < cursor) {
                    startIndex = i;
                    break;
                }
            }
        }

        // 결과 리스트 생성
        List<NotificationInfo> result = new ArrayList<>();
        int endIndex = Math.min(startIndex + limit, filteredList.size());

        if (startIndex < filteredList.size()) {
            result = filteredList.subList(startIndex, endIndex);
        }

        // 다음 페이지 존재 여부 및 커서 값 설정
        boolean hasNext = endIndex < filteredList.size();
        String nextCursor = hasNext ? String.valueOf(filteredList.get(endIndex - 1).getNotificationId()) : "";

        return ResponseEntity.ok(
                NotificationResponse.builder()
                        .notifications(result)
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .build()
        );
    }

    // 알림 읽음 처리
    @Operation(summary = "Mock 알림 읽음 처리", description = "테스트용 Mock 알림을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Mock 알림 읽음 처리 성공")})
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationReadResponse> readNotification(@PathVariable Long notificationId) {
        // 실제 알림 찾기
        for (NotificationInfo notification : mockNotifications) {
            if (notification.getNotificationId().equals(notificationId)) {
                // 읽음 상태로 변경
                notification.setIsRead(true);

                // 응답 반환
                return ResponseEntity.ok(
                        NotificationReadResponse.builder()
                                .type(notification.getType())
                                .targetId(notification.getTargetId())
                                .senderId(notification.getSenderId())
                                .build()
                );
            }
        }

        // 알림이 없는 경우 기본 응답
        return ResponseEntity.ok(
                NotificationReadResponse.builder()
                        .type(NotificationType.GUESTBOOK)
                        .targetId(1L)
                        .senderId(2L)
                        .build()
        );
    }
}