package com.roome.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockUserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Spring Security 필터 비활성화
class MockUserControllerTest {

    private static final String BASE_URL = "/mock/users";
    private static final Long MOCK_USER_ID = 999L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getMockUserProfile_success() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc
                .perform(get(BASE_URL + "/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nickname").value("Mock User " + userId))
                .andExpect(jsonPath("$.profileImage").exists())
                .andExpect(jsonPath("$.bio").exists())
                .andExpect(jsonPath("$.bookGenres").isArray())
                .andExpect(jsonPath("$.musicGenres").isArray())
                .andExpect(jsonPath("$.recommendedUsers").isArray());
    }

    @Test
    @DisplayName("프로필 수정 - 성공")
    void updateMockUserProfile_success() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest
                .builder()
                .nickname("Updated Nickname")
                .bio("Updated bio")
                .build();

        // when & then
        mockMvc
                .perform(patch(BASE_URL + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MOCK_USER_ID.toString()))
                .andExpect(jsonPath("$.nickname").value(request.getNickname()))
                .andExpect(jsonPath("$.bio").value(request.getBio()));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - POST 성공")
    void uploadMockProfileImage_post_success() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image content".getBytes()
        );

        // when & then
        mockMvc
                .perform(multipart(BASE_URL + "/profile/image")
                        .file(imageFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.fileName").value(imageFile.getOriginalFilename()));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - PUT 성공")
    void uploadMockProfileImage_put_success() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image content".getBytes()
        );

        // when & then
        mockMvc
                .perform(multipart(BASE_URL + "/profile/image")
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.fileName").value(imageFile.getOriginalFilename()));
    }

    @Test
    @DisplayName("프로필 이미지 삭제 - 성공")
    void deleteMockProfileImage_success() throws Exception {
        // given
        String imageUrl = "https://roome-profile-images.s3.amazonaws.com/profile/image.jpg";

        // when & then
        mockMvc
                .perform(delete(BASE_URL + "/profile/image")
                        .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(imageUrl));
    }
}