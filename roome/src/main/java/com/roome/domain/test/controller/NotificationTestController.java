package com.roome.domain.test.controller;

import com.roome.domain.event.notificationEvent.EventUpcomingNotificationEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(name = "테스트 API", description = "개발 테스트용 API 모음")
public class NotificationTestController {

    private final ApplicationEventPublisher eventPublisher;

    @Operation(
            summary = "이벤트 알림 테스트",
            description = "특정 사용자에게 이벤트 알림을 전송합니다. 알림 시스템 동작 테스트용입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 발송 성공")
            }
    )
    @PostMapping("/notifications/event")
    public ResponseEntity<String> testEventNotification(
            @Parameter(description = "알림을 받을 사용자 ID", required = true)
            @RequestParam Long userId,

            @Parameter(description = "알림에 포함될 이벤트 ID (미입력 시 기본값 0)")
            @RequestParam(required = false) Long eventId) {

        eventPublisher.publishEvent(new EventUpcomingNotificationEvent(
                this,
                0L,  // 시스템 사용자
                userId,
                eventId != null ? eventId : 0L
        ));

        return ResponseEntity.ok("테스트 알림이 발송되었습니다.");
    }
}