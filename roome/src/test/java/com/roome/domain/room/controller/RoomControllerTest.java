package com.roome.domain.room.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.UpdateRoomThemeRequestDto;
import com.roome.domain.room.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = RoomController.class)
public class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("방 아이디로 방 정보를 조회할 수 있다.")
    @WithMockUser
    @Test
    void getRoomById() throws Exception {
        Long roomId = 1L;
        Long userId = 1L;
        RoomResponseDto response = RoomResponseDto.builder()
                .roomId(roomId)
                .userId(userId)
                .theme("BASIC")
                .createdAt(LocalDateTime.now())
                .furnitures(Collections.emptyList())
                .storageLimits(null)
                .userStorage(null)
                .build();

        given(roomService.getRoomById(roomId)).willReturn(response);

        mockMvc.perform(get("/api/rooms/{roomId}", roomId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.theme").value("BASIC"));
    }

    @DisplayName("유저 아이디로 방 정보를 조회할 수 있다.")
    @Test
    void getRoomByUserId() throws Exception {
        Long userId = 1L;
        Long roomId = 2L;
        RoomResponseDto response = RoomResponseDto.builder()
                .roomId(roomId)
                .userId(userId)
                .theme("BASIC")
                .createdAt(LocalDateTime.now())
                .furnitures(Collections.emptyList())
                .storageLimits(null)
                .userStorage(null)
                .build();

        given(roomService.getRoomByUserId(userId)).willReturn(response);

        mockMvc.perform(get("/api/rooms")
                        .with(csrf())
                        .with(authentication(new TestingAuthenticationToken(userId, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.theme").value("BASIC"));
    }


    @DisplayName("방의 테마를 업데이트할 수 있다.")
    @Test
    void updateRoomTheme() throws Exception {
        Long userId = 1L;
        Long roomId = 1L;
        String newTheme = "MARINE";
        given(roomService.updateRoomTheme(userId, roomId, newTheme)).willReturn(newTheme);

        UpdateRoomThemeRequestDto requestDto = new UpdateRoomThemeRequestDto(newTheme);

        mockMvc.perform(put("/api/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf())
                        .with(authentication(new TestingAuthenticationToken(userId, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.updatedTheme").value(newTheme));

        verify(roomService).updateRoomTheme(userId, roomId, newTheme);
    }
}
