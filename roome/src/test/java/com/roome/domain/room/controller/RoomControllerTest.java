package com.roome.domain.room.controller;


import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.StorageLimitsDto;
import com.roome.domain.room.dto.UserStorageDto;
import com.roome.domain.room.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    private static final String BASE_URL = "/api/rooms";

    private RoomResponseDto mockRoomResponse;

    @BeforeEach
    void setUp() {
        mockRoomResponse = RoomResponseDto.builder()
                .roomId(1L)
                .userId(1L)
                .theme("basic")
                .createdAt(LocalDateTime.now())
                .furnitures(List.of(new FurnitureResponseDto("BOOKSHELF", true, 3, 100)))
                .storageLimits(new StorageLimitsDto(100, 100))
                .userStorage(new UserStorageDto(10L, 5L, 3L, 2L))
                .build();
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("방 ID로 방을 정상적으로 조회할 수 있다")
    void getRoomById_Success() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(mockRoomResponse);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.theme").value("basic"));
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("사용자의 방을 정상적으로 조회할 수 있다")
    void getRoomByUserId_Success() throws Exception {
        when(roomService.getRoomByUserId(1L)).thenReturn(mockRoomResponse);

        mockMvc.perform(get(BASE_URL).param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.theme").value("basic"));
    }


    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("방 테마를 정상적으로 변경할 수 있다")
    void updateRoomTheme_Success() throws Exception {
        when(roomService.updateRoomTheme(1L, 1L, "vintage")).thenReturn("vintage");

        mockMvc.perform(put(BASE_URL + "/1")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"themeName\":\"vintage\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.updatedTheme").value("vintage"));
    }


    @Test
    @WithMockUser(username = "1", roles = {"USER"})
    @DisplayName("가구 상태를 정상적으로 변경할 수 있다")
    void toggleFurnitureVisibility_Success() throws Exception {
        FurnitureResponseDto updatedFurniture = new FurnitureResponseDto("BOOKSHELF", false, 3, 100);
        when(roomService.toggleFurnitureVisibility(1L, 1L, "BOOKSHELF")).thenReturn(updatedFurniture);

        mockMvc.perform(put(BASE_URL + "/1/furniture")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"furnitureType\":\"BOOKSHELF\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.furniture.furnitureType").value("BOOKSHELF"))
                .andExpect(jsonPath("$.furniture.isVisible").value(false));
    }

}
