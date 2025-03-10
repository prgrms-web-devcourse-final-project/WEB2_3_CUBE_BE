package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.NotificationInfo;
import com.roome.domain.notification.dto.NotificationReadResponse;
import com.roome.domain.notification.dto.NotificationResponse;
import com.roome.domain.notification.dto.NotificationType;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Tag(name = "Mock Notification", description = "알림 관련 Mock API - 개발 테스트용")
@Slf4j
@Validated
@RestController
@RequestMapping("/mock/notifications")
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
      LocalDateTime createdTime = LocalDateTime.now().minusHours(100L - i);

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

  @Operation(summary = "Mock 알림 목록 조회",
      description = "테스트용 Mock 알림 목록을 무한 스크롤 방식으로 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock 알림 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 limit 값 (INVALID_LIMIT_VALUE)"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 cursor 값 (INVALID_CURSOR_VALUE)")
  })
  @GetMapping
  public ResponseEntity<NotificationResponse> getNotifications(
      @Parameter(description = "페이지네이션 커서 (마지막으로 받은 알림 ID)", example = "10")
      @RequestParam(required = false) Long cursor,
      @Parameter(description = "한 페이지당 조회할 알림 수(1-100)", example = "10")
      @RequestParam(defaultValue = "10") @Max(100) @Min(1) int limit,
      @Parameter(description = "읽음 상태로 필터링 (true: 읽은 알림, false: 읽지 않은 알림, null: 모든 알림)", example = "false")
      @RequestParam(required = false) Boolean read) {

    // 필터링을 위한 임시 리스트 (이미 ID 내림차순으로 정렬됨)
    List<NotificationInfo> filteredList = new ArrayList<>(mockNotifications);

    // 읽음 상태로 필터링
    if (read != null) {
      filteredList = filteredList.stream()
          .filter(notification -> notification.getIsRead().equals(read))
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
    String nextCursor =
        hasNext ? String.valueOf(filteredList.get(endIndex - 1).getNotificationId()) : "";

    return ResponseEntity.ok(
        NotificationResponse.builder()
            .notifications(result)
            .nextCursor(nextCursor)
            .hasNext(hasNext)
            .build()
    );
  }

  @Operation(summary = "Mock 알림 읽음 처리",
      description = "테스트용 Mock 알림을 읽음 상태로 변경합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock 알림 읽음 처리 성공"),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 알림 ID (INVALID_CURSOR_VALUE)"),
      @ApiResponse(responseCode = "403", description = "알림에 대한 접근 권한 없음 (NOTIFICATION_ACCESS_DENIED)"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 (NOTIFICATION_NOT_FOUND)"),
      @ApiResponse(responseCode = "400", description = "이미 읽음 처리된 알림 (NOTIFICATION_ALREADY_READ)")
  })
  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<NotificationReadResponse> readNotification(
      @Parameter(description = "읽음 처리할 알림의 ID", example = "1")
      @PathVariable Long notificationId) {

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