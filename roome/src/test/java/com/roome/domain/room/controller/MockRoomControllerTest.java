package com.roome.domain.room.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockRoomController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class MockRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockRoomController mockRoomController;

    private static final String BASE_URL = "/mock/rooms";

    @Test
    @DisplayName("mockRoomController가 정상적으로 주입된다")
    void testRequiredArgsConstructor() {
        assert mockRoomController != null;
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("Mock 데이터로 방을 조회할 수 있다")
    void getRoomById_Success() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.userId").value(67890L))
                .andExpect(jsonPath("$.theme").value("basic"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("Mock 데이터로 사용자 ID로 방을 조회할 수 있다")
    void getRoomByUserId_Success() throws Exception {
        mockMvc.perform(get(BASE_URL).param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.theme").value("forest"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("Mock 데이터로 방 테마를 변경할 수 있다")
    void updateRoomTheme_Success() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"themeName\":\"marine\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.updatedTheme").value("marine"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("Mock 데이터로 가구 상태를 변경할 수 있다")
    void toggleFurnitureVisibility_Success() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1/furniture")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"furnitureType\":\"BOOKSHELF\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.furnitureType").value("BOOKSHELF"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("가구 상태 변경 시 furnitureType이 없는 경우 400 오류를 반환한다")
    void toggleFurnitureVisibility_FurnitureTypeMissing() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1/furniture")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("가구 상태 변경 시 잘못된 furnitureType이 제공되면 400 오류를 반환한다")
    void toggleFurnitureVisibility_InvalidFurnitureType() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1/furniture")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"furnitureType\":\"INVALID_TYPE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("가구 상태 변경 시 해당 가구가 존재하지 않으면 400 오류를 반환한다")
    void toggleFurnitureVisibility_FurnitureNotFound() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1/furniture")
                        .param("userId", "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"furnitureType\":\"TABLE\"}"))
                .andExpect(status().isBadRequest());
    }


}