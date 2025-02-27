package com.roome.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.notification.dto.NotificationInfo;
import com.roome.domain.notification.dto.NotificationReadResponse;
import com.roome.domain.notification.dto.NotificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class MockNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("전체 알림 목록 조회 API 테스트")
    void testGetAllNotifications() throws Exception {
        // GET 요청 수행
        MvcResult result = mockMvc.perform(get("/mock/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 파싱
        String content = result.getResponse().getContentAsString();
        NotificationResponse response = objectMapper.readValue(content, NotificationResponse.class);

        // 검증
        assertNotNull(response);
        assertNotNull(response.getNotifications());
        assertEquals(10, response.getNotifications().size()); // 기본 limit은 10
        assertTrue(response.isHasNext()); // 100개 중 10개만 가져왔으므로 다음 페이지 존재
        assertFalse(response.getNextCursor().isEmpty());
    }

    @Test
    @DisplayName("읽은 알림만 조회 API 테스트")
    void testGetReadNotifications() throws Exception {
        // GET 요청 수행 (read=true 파라미터 추가)
        MvcResult result = mockMvc.perform(get("/mock/notifications")
                        .param("read", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 파싱
        String content = result.getResponse().getContentAsString();
        NotificationResponse response = objectMapper.readValue(content, NotificationResponse.class);

        // 검증
        assertNotNull(response);
        List<NotificationInfo> notifications = response.getNotifications();
        assertFalse(notifications.isEmpty());

        // 모든 알림이 읽음 상태인지 확인
        for (NotificationInfo notification : notifications) {
            assertTrue(notification.getIsRead());
        }
    }

    @Test
    @DisplayName("읽지 않은 알림만 조회 API 테스트")
    void testGetUnreadNotifications() throws Exception {
        // GET 요청 수행 (read=false 파라미터 추가)
        MvcResult result = mockMvc.perform(get("/mock/notifications")
                        .param("read", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 파싱
        String content = result.getResponse().getContentAsString();
        NotificationResponse response = objectMapper.readValue(content, NotificationResponse.class);

        // 검증
        assertNotNull(response);
        List<NotificationInfo> notifications = response.getNotifications();
        assertFalse(notifications.isEmpty());

        // 모든 알림이 읽지 않음 상태인지 확인
        for (NotificationInfo notification : notifications) {
            assertFalse(notification.getIsRead());
        }
    }

    @Test
    @DisplayName("커서 기반 페이징 API 테스트")
    void testCursorBasedPaging() throws Exception {
        // 첫 페이지 요청
        MvcResult firstPageResult = mockMvc.perform(get("/mock/notifications")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String firstPageContent = firstPageResult.getResponse().getContentAsString();
        NotificationResponse firstPageResponse = objectMapper.readValue(firstPageContent, NotificationResponse.class);

        assertNotNull(firstPageResponse);
        assertFalse(firstPageResponse.getNotifications().isEmpty());
        assertEquals(5, firstPageResponse.getNotifications().size());
        String nextCursor = firstPageResponse.getNextCursor();
        assertFalse(nextCursor.isEmpty());

        // 두 번째 페이지 요청 (첫 페이지의 nextCursor 사용)
        MvcResult secondPageResult = mockMvc.perform(get("/mock/notifications")
                        .param("cursor", nextCursor)
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String secondPageContent = secondPageResult.getResponse().getContentAsString();
        NotificationResponse secondPageResponse = objectMapper.readValue(secondPageContent, NotificationResponse.class);

        assertNotNull(secondPageResponse);
        assertFalse(secondPageResponse.getNotifications().isEmpty());
        assertEquals(5, secondPageResponse.getNotifications().size());

        // 두 페이지의 첫 번째 알림 ID가 다른지 확인
        Long firstPageFirstId = firstPageResponse.getNotifications().get(0).getNotificationId();
        Long secondPageFirstId = secondPageResponse.getNotifications().get(0).getNotificationId();
        assertNotEquals(firstPageFirstId, secondPageFirstId);
    }

    @Test
    @DisplayName("알림 읽음 처리 API 테스트")
    void testReadNotification() throws Exception {
        // 읽지 않은 알림 ID (홀수) 선택
        Long notificationId = 1L;

        // PATCH 요청 수행
        MvcResult result = mockMvc.perform(patch("/mock/notifications/{notificationId}/read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 파싱
        String content = result.getResponse().getContentAsString();
        NotificationReadResponse response = objectMapper.readValue(content, NotificationReadResponse.class);

        // 응답 검증
        assertNotNull(response);
        assertNotNull(response.getType());
        assertNotNull(response.getTargetId());
        assertNotNull(response.getSenderId());

        // 해당 ID로 직접 요청하여 알림이 읽음 상태로 변경되었는지 확인
        MvcResult getResult = mockMvc.perform(get("/mock/notifications")
                        .param("read", "true") // 읽음 상태인 알림만 가져오기
                        .param("limit", "100") // 모든 알림 가져오기
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String getContent = getResult.getResponse().getContentAsString();
        NotificationResponse getResponse = objectMapper.readValue(getContent, NotificationResponse.class);

        // 해당 ID의 알림이 읽음 상태인 목록에 있는지 확인
        boolean isRead = getResponse.getNotifications().stream()
                .anyMatch(n -> n.getNotificationId().equals(notificationId));

        assertTrue(isRead);
    }

    @Test
    @DisplayName("존재하지 않는 알림 읽음 처리 API 테스트")
    void testReadNonExistingNotification() throws Exception {
        // 존재하지 않는 알림 ID
        Long notificationId = 999L;

        // PATCH 요청 수행
        MvcResult result = mockMvc.perform(patch("/mock/notifications/{notificationId}/read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 파싱
        String content = result.getResponse().getContentAsString();
        NotificationReadResponse response = objectMapper.readValue(content, NotificationReadResponse.class);

        // 기본 응답 검증
        assertNotNull(response);
        assertNotNull(response.getType());
        assertEquals(1L, response.getTargetId());
        assertEquals(2L, response.getSenderId());
    }
}