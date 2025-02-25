package com.roome.domain.guestbook.controller;

import com.roome.domain.guestbook.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MockGuestbookControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MockGuestbookController()).build();
    }

    @Test
    public void testGetMockGuestbook_Success() throws Exception {
        Long roomId = 1L;
        int page = 1;
        int size = 10;

        // Mock 데이터 설정
        GuestbookResponseDto guestbook1 = new GuestbookResponseDto(1L, 123L, "VisitorA", "https://example.com/profileA.jpg", "방 정말 예쁘네요!", LocalDateTime.parse("2025-02-20T12:00:00"), "하우스메이트");
        GuestbookResponseDto guestbook2 = new GuestbookResponseDto(2L, 124L, "VisitorB", "https://example.com/profileB.jpg", "분위기가 너무 좋아요!", LocalDateTime.parse("2025-02-20T12:10:00"), "지나가던 나그네");

        // Mock 호출 설정
        mockMvc.perform(get("/mock/guestbooks/{roomId}", roomId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.guestbook[0].message").value("방 정말 예쁘네요!"))
                .andExpect(jsonPath("$.guestbook[1].message").value("분위기가 너무 좋아요!"))
                .andExpect(jsonPath("$.pagination.page").value(page))
                .andExpect(jsonPath("$.pagination.size").value(size));
    }

    @Test
    public void testAddMockGuestbook_Success() throws Exception {
        Long roomId = 1L;
        String message = "Nice place!";
        GuestbookRequestDto requestDto = new GuestbookRequestDto(message);

        // 방명록 추가 테스트
        mockMvc.perform(post("/mock/guestbooks/{roomId}", roomId)
                        .contentType("application/json")
                        .content("{\"message\":\"" + message + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.nickname").value("VisitorC"))
                .andExpect(jsonPath("$.profileImage").value("https://example.com/profileC.jpg"));
    }

    @Test
    public void testDeleteMockGuestbook_Success() throws Exception {
        Long guestbookId = 1L;

        mockMvc.perform(delete("/mock/guestbooks/{guestbookId}", guestbookId))
                .andDo(print()) // 실제 응답을 출력해서 확인
                .andExpect(status().isOk())
                .andExpect(content().string("ID: " + guestbookId));
    }
}