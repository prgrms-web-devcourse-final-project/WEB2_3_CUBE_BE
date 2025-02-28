package com.roome.domain.notification.controller;

import com.roome.domain.notification.dto.NotificationType;
import com.roome.domain.notification.entity.Notification;
import com.roome.domain.notification.repository.NotificationRepository;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.user.temp.UserPrincipal;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)  // Spring Security 필터 비활성화
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User senderUser;
    private UserPrincipal mockUserPrincipal;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성 및 저장
        testUser = User.builder()
                .email("test@example.com")
                .name("Test User")
                .nickname("TestUser")
                .profileImage("test.jpg")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("test123")
                .build();

        testUser = userRepository.save(testUser);

        // 알림 발신자 사용자 생성 및 저장
        senderUser = User.builder()
                .email("sender@example.com")
                .name("Sender User")
                .nickname("SenderUser")
                .profileImage("sender.jpg")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("sender123")
                .build();

        senderUser = userRepository.save(senderUser);

        // UserPrincipal 생성
        mockUserPrincipal = new UserPrincipal(testUser);

        // SecurityContext에 인증된 사용자 정보 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                mockUserPrincipal,
                null,
                mockUserPrincipal.getAuthorities()
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotifications_Success() throws Exception {
        // given
        // 알림 데이터 생성 및 저장
        createTestNotification(NotificationType.GUESTBOOK, false);

        // when & then
        mockMvc
                .perform(get("/api/notifications").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications[0].type").value("GUESTBOOK"))
                .andExpect(jsonPath("$.notifications[0].senderId").value(senderUser.getId()))
                .andDo(print());
    }

    @Test
    @DisplayName("유효하지 않은 cursor 값으로 알림 목록 조회 실패")
    void getNotifications_InvalidCursor_Fail() throws Exception {
        // when & then
        mockMvc
                .perform(get("/api/notifications")
                        .param("cursor", "-1")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 cursor 값입니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("유효하지 않은 limit 값(하한)으로 알림 목록 조회 실패")
    void getNotifications_InvalidLimitLower_Fail() throws Exception {
        // when & then
        mockMvc
                .perform(get("/api/notifications")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 limit 값입니다. (1-100 사이의 값을 입력해주세요)"))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("유효하지 않은 limit 값(상한)으로 알림 목록 조회 실패")
    void getNotifications_InvalidLimitUpper_Fail() throws Exception {
        // when & then
        mockMvc
                .perform(get("/api/notifications")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 limit 값입니다. (1-100 사이의 값을 입력해주세요)"))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void readNotification_Success() throws Exception {
        // given
        Notification notification = createTestNotification(NotificationType.GUESTBOOK, false);

        // when & then
        mockMvc
                .perform(patch("/api/notifications/{notificationId}/read", notification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("GUESTBOOK"))
                .andExpect(jsonPath("$.targetId").value(3))
                .andExpect(jsonPath("$.senderId").value(senderUser.getId()))
                .andDo(print());
    }

    @Test
    @DisplayName("유효하지 않은 notificationId로 알림 읽음 처리 실패")
    void readNotification_InvalidId_Fail() throws Exception {
        // given
        Long invalidNotificationId = -1L;

        // when & then
        mockMvc
                .perform(patch("/api/notifications/{notificationId}/read", invalidNotificationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 cursor 값입니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("이미 읽은 알림을 다시 읽음 처리하는 경우 실패")
    void readNotification_AlreadyRead_Fail() throws Exception {
        // given
        Notification notification = createTestNotification(NotificationType.GUESTBOOK);

        // when & then
        mockMvc
                .perform(patch("/api/notifications/{notificationId}/read", notification.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 읽음 처리된 알림입니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 알림에 대한 읽음 처리 실패")
    void readNotification_NotFound_Fail() throws Exception {
        // given
        Long notificationId = 999L;

        // when & then
        mockMvc
                .perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.NOTIFICATION_NOT_FOUND.getStatus().value()))
                .andDo(print());
    }

    @Test
    @DisplayName("권한이 없는 알림에 대한 읽음 처리 실패")
    void readNotification_AccessDenied_Fail() throws Exception {
        // given
        // 다른 사용자의 알림 생성
        User otherUser = User.builder()
                .email("other@example.com")
                .name("Other User")
                .nickname("OtherUser")
                .profileImage("other.jpg")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("other123")
                .build();

        otherUser = userRepository.save(otherUser);

        Notification otherUserNotification = Notification.builder()
                .type(NotificationType.GUESTBOOK)
                .senderId(senderUser.getId())
                .receiverId(otherUser.getId())  // 다른 사용자를 수신자로 설정
                .targetId(3L)
                .build();

        otherUserNotification = notificationRepository.save(otherUserNotification);

        // when & then
        mockMvc
                .perform(patch("/api/notifications/{notificationId}/read", otherUserNotification.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 알림에 접근 권한이 없습니다."))
                .andExpect(jsonPath("$.code").value(403))
                .andDo(print());
    }

    // 테스트용 알림 데이터 생성 헬퍼 메서드
    private Notification createTestNotification(NotificationType type, boolean isRead) {
        Notification notification = Notification.builder()
                .type(type)
                .senderId(senderUser.getId())
                .receiverId(testUser.getId())
                .targetId(3L)
                .build();

        if (isRead) {
            notification.markAsRead();
        }

        return notificationRepository.save(notification);
    }

    // Overloaded method for backward compatibility
    private Notification createTestNotification(NotificationType type) {
        return createTestNotification(type, true);
    }
}