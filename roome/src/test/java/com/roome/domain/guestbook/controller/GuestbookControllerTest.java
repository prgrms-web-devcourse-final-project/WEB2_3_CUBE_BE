package com.roome.domain.guestbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.guestbook.dto.GuestbookListResponseDto;
import com.roome.domain.guestbook.dto.GuestbookRequestDto;
import com.roome.domain.guestbook.dto.GuestbookResponseDto;
import com.roome.domain.guestbook.dto.PaginationDto;
import com.roome.domain.guestbook.service.GuestbookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = com.roome.domain.guestbook.controller.GuestbookController.class)
public class GuestbookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestbookService guestbookService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("방 아이디로 게스트북 리스트를 조회할 수 있다.")
    @WithMockUser
    @Test
    void getGuestbook() throws Exception {
        Long roomId = 1L;
        int page = 0;
        int size = 10;

        GuestbookResponseDto entry = GuestbookResponseDto.builder()
                .guestbookId(1L)
                .userId(1L)
                .nickname("John")
                .profileImage("profile.jpg")
                .message("Hello guestbook!")
                .createdAt(LocalDateTime.now())
                .relation("FRIEND")
                .build();

        PaginationDto pagination = PaginationDto.builder()
                .page(page)
                .size(size)
                .totalPages(1)
                .build();

        GuestbookListResponseDto responseDto = GuestbookListResponseDto.builder()
                .roomId(roomId)
                .guestbook(List.of(entry))
                .pagination(pagination)
                .build();

        given(guestbookService.getGuestbook(roomId, page, size)).willReturn(responseDto);

        mockMvc.perform(get("/api/guestbooks/{roomId}", roomId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                // guestbook 리스트의 첫 번째 엔트리 검증
                .andExpect(jsonPath("$.guestbook[0].guestbookId").value(1))
                .andExpect(jsonPath("$.guestbook[0].nickname").value("John"))
                .andExpect(jsonPath("$.guestbook[0].profileImage").value("profile.jpg"))
                .andExpect(jsonPath("$.guestbook[0].message").value("Hello guestbook!"))
                .andExpect(jsonPath("$.guestbook[0].relation").value("FRIEND"))
                // Pagination 검증
                .andExpect(jsonPath("$.pagination.page").value(page))
                .andExpect(jsonPath("$.pagination.size").value(size))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @DisplayName("게스트북에 글을 추가할 수 있다.")
    @WithMockUser
    @Test
    void addGuestbook() throws Exception {
        Long roomId = 1L;
        Long userId = 1L;

        GuestbookRequestDto requestDto = new GuestbookRequestDto();
        String jsonRequest = "{\"message\":\"Test guestbook message\"}";

        GuestbookResponseDto responseDto = GuestbookResponseDto.builder()
                .guestbookId(1L)
                .userId(userId)
                .nickname("John")
                .profileImage("profile.jpg")
                .message("Test guestbook message")
                .createdAt(LocalDateTime.now())
                .relation("FRIEND")
                .build();

        given(guestbookService.addGuestbook(roomId, userId, requestDto))
                .willReturn(responseDto);

        mockMvc.perform(post("/api/guestbooks/{roomId}", roomId)
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestbookId").value(1))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.nickname").value("John"))
                .andExpect(jsonPath("$.profileImage").value("profile.jpg"))
                .andExpect(jsonPath("$.message").value("Test guestbook message"))
                .andExpect(jsonPath("$.relation").value("FRIEND"));

        verify(guestbookService).addGuestbook(roomId, userId, requestDto);
    }

    @DisplayName("게스트북 글을 삭제할 수 있다.")
    @WithMockUser
    @Test
    void deleteGuestbook() throws Exception {
        Long guestbookId = 1L;
        Long userId = 1L;

        mockMvc.perform(delete("/api/guestbooks/{guestbookId}", guestbookId)
                        .param("userId", String.valueOf(userId))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(guestbookService).deleteGuestbook(guestbookId, userId);
    }
}
