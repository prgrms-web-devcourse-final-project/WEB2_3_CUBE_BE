package com.roome.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.notification.dto.*;
import com.roome.domain.notification.service.NotificationService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.temp.UserPrincipal;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)  // Spring Security 필터 비활성화
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal mockUserPrincipal;

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        User mockUser = User
                .builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .nickname("TestUser")
                .profileImage("test.jpg")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("test123")
                .build();

        // UserPrincipal 생성
        mockUserPrincipal = new UserPrincipal(mockUser);
        // SecurityContext에 인증된 사용자 정보 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(mockUserPrincipal, null,
                                                                                mockUserPrincipal.getAuthorities());
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotifications_Success() throws Exception {
        // given
        List<NotificationInfo> notifications = Arrays.asList(NotificationInfo
                                                                     .builder()
                                                                     .notificationId(1L)
                                                                     .type(NotificationType.GUESTBOOK)
                                                                     .senderId(2L)
                                                                     .senderNickName("Test User")
                                                                     .senderProfileImage("test.jpg")
                                                                     .targetId(3L)
                                                                     .createdAt(LocalDateTime.now())
                                                                     .build());

        NotificationResponse response = NotificationResponse
                .builder()
                .notifications(notifications)
                .nextCursor(null)
                .hasNext(false)
                .build();

        when(notificationService.getNotifications(any(NotificationSearchCondition.class))).thenReturn(response);

        // when & then
        mockMvc
                .perform(get("/api/notifications").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications[0].notificationId").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void readNotification_Success() throws Exception {
        // given
        Long notificationId = 1L;
        NotificationReadResponse response = NotificationReadResponse
                .builder()
                .type(NotificationType.GUESTBOOK)
                .targetId(2L)
                .senderId(3L)
                .build();

        when(notificationService.readNotification(eq(notificationId), any())).thenReturn(response);

        // when & then
        mockMvc
                .perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("GUESTBOOK"))
                .andExpect(jsonPath("$.targetId").value(2))
                .andExpect(jsonPath("$.senderId").value(3))
                .andDo(print());
    }

    @Test
    @DisplayName("이미 읽은 알림을 다시 읽음 처리하는 경우 실패")
    void readNotification_AlreadyRead_Fail() throws Exception {
        // given
        Long notificationId = 1L;

        // notificationService.readNotification 메서드가 이미 읽은 알림에 대해 예외를 던지도록 설정
        when(notificationService.readNotification(eq(notificationId), any()))
                .thenThrow(new BusinessException(ErrorCode.NOTIFICATION_ALREADY_READ));

        // when & then
        mockMvc
                .perform(patch("/api/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 읽음 처리된 알림입니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }
}