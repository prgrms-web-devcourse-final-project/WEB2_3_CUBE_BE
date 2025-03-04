package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.domain.notification.dto.NotificationWebSocketMessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// 이 컨트롤러는 실제 API 엔드포인트가 아닌 웹소켓 연결에 대한 문서화를 위한 더미 컨트롤러입니다.
/// Swagger UI에 웹소켓 사용 방법을 표시하기 위해 존재합니다.
@RestController
@RequestMapping("/docs")
@Tag(name = "WebSocket 문서", description = "웹소켓 연결 및 사용 방법에 관한 문서")
public class WebSocketDocumentationController {

    @GetMapping("/websocket")
    @Operation(
            summary = "WebSocket 연결 정보",
            description = "이것은 실제 API가 아닌 웹소켓 연결에 관한 정보를 제공하는 문서입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "WebSocket 연결 정보",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public Map<String, Object> getWebSocketConnectionInfo() {
        Map<String, Object> info = new HashMap<>();

        // 웹소켓 연결 정보
        Map<String, Object> connection = new HashMap<>();
        connection.put("endpoint", "/ws");
        connection.put("protocols", "STOMP, SockJS");
        connection.put("authentication", "Bearer JWT token in Authorization header");

        List<String> allowedOrigins = Arrays.asList(
                "https://desqb38rc2v50.cloudfront.net",
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:63342"
        );
        connection.put("allowed_origins", allowedOrigins);

        // 가능한 에러 코드
        Map<String, String> errorCodes = new HashMap<>();
        errorCodes.put("WEBSOCKET_TOKEN_MISSING", "JWT 토큰이 누락됨");
        errorCodes.put("WEBSOCKET_TOKEN_INVALID", "유효하지 않은 JWT 토큰");
        errorCodes.put("WEBSOCKET_TOKEN_BLACKLISTED", "블랙리스트에 등록된 토큰");
        errorCodes.put("INVALID_USER_ID_FORMAT", "토큰의 userId 형식이 올바르지 않음");

        // 구독 정보
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("personal_notifications", "/user/{userId}/notification");

        // 메시지 예시
        NotificationWebSocketMessageDto exampleMessage = NotificationWebSocketMessageDto.builder()
                .notificationId(1L)
                .type(NotificationType.HOUSE_MATE)
                .receiverId(123L).build();

        // JavaScript 사용 예시
        String jsExample =
                "// SockJS와 STOMP 클라이언트 라이브러리 필요\n" +
                        "const socket = new SockJS('/ws');\n" +
                        "const stompClient = Stomp.over(socket);\n\n" +

                        "// JWT 토큰을 헤더에 포함\n" +
                        "const headers = {\n" +
                        "  'Authorization': 'Bearer ' + jwtToken\n" +
                        "};\n\n" +

                        "// 연결 시도\n" +
                        "stompClient.connect(headers, () => {\n" +
                        "  // 사용자별 알림 구독\n" +
                        "  stompClient.subscribe('/user/' + userId + '/notification', (payload) => {\n" +
                        "    const notification = JSON.parse(payload.body);\n" +
                        "    console.log('알림 수신:', notification);\n" +
                        "  });\n" +
                        "}, (error) => {\n" +
                        "  console.error('연결 오류:', error);\n" +
                        "});\n\n" +

                        "// 연결 해제\n" +
                        "function disconnect() {\n" +
                        "  if (stompClient !== null) {\n" +
                        "    stompClient.disconnect();\n" +
                        "    console.log('WebSocket 연결 해제');\n" +
                        "  }\n" +
                        "}";

        // 모든 정보 합치기
        info.put("connection", connection);
        info.put("error_codes", errorCodes);
        info.put("subscription", subscription);
        info.put("message_example", exampleMessage);
        info.put("javascript_example", jsExample);
        info.put("note", "이 엔드포인트는 정보 제공용이며 실제 API가 아닙니다. 웹소켓 연결을 위해 위 정보를 참고하세요.");

        return info;
    }

    @GetMapping("/websocket/types")
    @Operation(
            summary = "알림 타입 정보",
            description = "WebSocket을 통해 전송되는 알림 타입 정보",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 타입 목록",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public Map<String, String> getNotificationTypes() {
        Map<String, String> types = new HashMap<>();
        for (NotificationType type : NotificationType.values()) {
            types.put(type.name(), getDescriptionForType(type));
        }
        return types;
    }

    private String getDescriptionForType(NotificationType type) {
        switch (type) {
            case EVENT:
                return "이벤트 알림";
            case GUESTBOOK:
                return "방명록 알림";
            case HOUSE_MATE:
                return "하우스 메이트 알림";
            case MUSIC_COMMENT:
                return "음악 댓글 알림";
            default:
                return "기타 알림";
        }
    }
}