//package com.roome.domain.notification.controller;
//
//import com.roome.domain.notification.dto.NotificationWebSocketMessageDto;
//import com.roome.domain.notification.service.NotificationWebSocketService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/notification/websocket/test")
//@Tag(name = "알림 테스트", description = "웹소켓 알림 시스템 테스트를 위한 API")
//public class NotificationWebSocketTestController {
//
//    private final NotificationWebSocketService notificationWebSocketService;
//
//    @PostMapping("/send")
//    @Operation(summary = "테스트 알림 전송", description = "특정 사용자에게 테스트 알림을 전송합니다")
//    public ResponseEntity<String> sendTestNotification(@RequestBody NotificationWebSocketMessageDto request) {
//        log.info("테스트 알림 요청: 수신자={}, 알림ID={}, 타입={}",
//                request.getReceiverId(), request.getNotificationId(), request.getType());
//
//        notificationWebSocketService.sendNotificationToUser(
//                request.getReceiverId(),
//                request.getNotificationId(),
//                request.getType()
//        );
//
//        return ResponseEntity.ok("테스트 알림이 성공적으로 전송되었습니다");
//    }
//}