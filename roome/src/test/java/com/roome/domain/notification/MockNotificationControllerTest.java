package com.roome.domain.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.notification.controller.MockNotificationController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MockNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class MockNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("알림 목록 조회 API 테스트")
    class GetNotificationsTest {

        @Test
        @DisplayName("읽지 않은 알림 목록을 조회한다")
        void getUnreadNotifications() throws Exception {
            // when & then
            mockMvc.perform(get("/mock/notifications")
                                    .param("cursor", (String) null)
                                    .param("limit", "10")
                                    .param("read", "false")
                                    .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.notifications").isArray())
                   .andExpect(jsonPath("$.notifications.length()").value(3))
                   .andExpect(jsonPath("$.notifications[0].notificationId").value(1))
                   .andExpect(jsonPath("$.notifications[0].read").value(false))
                   .andExpect(jsonPath("$.notifications[1].notificationId").value(3))
                   .andExpect(jsonPath("$.notifications[2].notificationId").value(5))
                   .andExpect(jsonPath("$.nextCursor").value(""))
                   .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("읽은 알림 목록을 조회한다")
        void getReadNotifications() throws Exception {
            // when & then
            mockMvc.perform(get("/mock/notifications")
                                    .param("cursor", (String) null)
                                    .param("limit", "10")
                                    .param("read", "true")
                                    .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.notifications").isArray())
                   .andExpect(jsonPath("$.notifications.length()").value(2))
                   .andExpect(jsonPath("$.notifications[0].notificationId").value(2))
                   .andExpect(jsonPath("$.notifications[0].read").value(true))
                   .andExpect(jsonPath("$.notifications[1].notificationId").value(4))
                   .andExpect(jsonPath("$.nextCursor").value(""))
                   .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("기본값으로 알림 목록을 조회한다")
        void getNotificationsWithDefaultParams() throws Exception {
            // when & then
            mockMvc.perform(get("/mock/notifications")
                                    .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.notifications").isArray())
                   .andExpect(jsonPath("$.notifications.length()").value(2))
                   .andExpect(jsonPath("$.notifications[0].read").value(true));
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리 API 테스트")
    class ReadNotificationTest {

        @Test
        @DisplayName("단일 알림을 읽음 처리한다")
        void readSingleNotification() throws Exception {
            // when & then
            mockMvc.perform(patch("/mock/notifications/{notificationId}/read", "1")
                                    .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.type").value("GUESTBOOK"))
                   .andExpect(jsonPath("$.targetId").value(1))
                   .andExpect(jsonPath("$.senderId").value(2));
        }
    }
}