package com.roome.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.global.exception.ControllerException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockUserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)  // Spring Security 필터 비활성화
class MockUserProfileControllerTest {

    private static final String BASE_URL = "/mock/users";
    private static final Long MOCK_USER_ID = 999L;
    private static final String MOCK_BUCKET_NAME = "roome-profile-images";

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
    @DisplayName("프로필 조회 - 음수 ID")
    void getMockUserProfile_negativeId() throws Exception {
        // given
        Long userId = -1L;

        // when & then
        mockMvc
                .perform(get(BASE_URL + "/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nickname").value("Mock User " + userId));
    }

    @Test
    @DisplayName("프로필 조회 - 0 ID")
    void getMockUserProfile_zeroId() throws Exception {
        // given
        Long userId = 0L;

        // when & then
        mockMvc
                .perform(get(BASE_URL + "/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nickname").value("Mock User " + userId));
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
    @DisplayName("프로필 이미지 업로드 - 다른 이미지 타입")
    void uploadMockProfileImage_differentImageType() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
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
    @DisplayName("프로필 이미지 업로드 - 빈 파일 (실패)")
    void uploadMockProfileImage_emptyFile() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        // when & then
        mockMvc
                .perform(multipart(BASE_URL + "/profile/image")
                        .file(emptyFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ControllerException))
                .andExpect(result -> {
                    ControllerException exception = (ControllerException) result.getResolvedException();
                    assertTrue(exception.getErrorCode() == ErrorCode.IMAGE_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - 파일 크기 초과 (실패)")
    void uploadMockProfileImage_fileSizeExceeded() throws Exception {
        // given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB, 제한은 5MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "image",
                "large.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent
        );

        // when & then
        mockMvc
                .perform(multipart(BASE_URL + "/profile/image")
                        .file(largeFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ControllerException))
                .andExpect(result -> {
                    ControllerException exception = (ControllerException) result.getResolvedException();
                    assertTrue(exception.getErrorCode() == ErrorCode.IMAGE_SIZE_EXCEEDED);
                });
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - 지원하지 않는 확장자 (실패)")
    void uploadMockProfileImage_unsupportedExtension() throws Exception {
        // given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text content".getBytes()
        );

        // when & then
        mockMvc
                .perform(multipart(BASE_URL + "/profile/image")
                        .file(invalidFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ControllerException))
                .andExpect(result -> {
                    ControllerException exception = (ControllerException) result.getResolvedException();
                    assertTrue(exception.getErrorCode() == ErrorCode.INVALID_IMAGE_FORMAT);
                });
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
    @DisplayName("프로필 이미지 업로드 - 확장자 없는 파일명")
    void uploadMockProfileImage_noExtension() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "no-extension",
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
                .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.containsString(".jpg")))
                .andExpect(jsonPath("$.fileName").value(imageFile.getOriginalFilename()));
    }

    @Test
    @DisplayName("프로필 이미지 삭제 - 성공")
    void deleteMockProfileImage_success() throws Exception {
        // given
        String imageUrl = "https://" + MOCK_BUCKET_NAME + ".s3.amazonaws.com/profile/1234567890abcdef1234567890abcdef.jpg";

        // when & then
        mockMvc
                .perform(delete(BASE_URL + "/profile/image")
                        .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(imageUrl));
    }

    @Test
    @DisplayName("프로필 이미지 삭제 - 빈 URL (실패)")
    void deleteMockProfileImage_emptyUrl() throws Exception {
        // given
        String imageUrl = "";

        // when & then
        mockMvc
                .perform(delete(BASE_URL + "/profile/image")
                        .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ControllerException))
                .andExpect(result -> {
                    ControllerException exception = (ControllerException) result.getResolvedException();
                    assertTrue(exception.getErrorCode() == ErrorCode.INVALID_IMAGE_URL);
                });
    }

    @Test
    @DisplayName("프로필 이미지 삭제 - 유효하지 않은 URL (실패)")
    void deleteMockProfileImage_invalidUrl() throws Exception {
        // given
        String imageUrl = "invalid-url";

        // when & then
        mockMvc
                .perform(delete(BASE_URL + "/profile/image")
                        .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ControllerException))
                .andExpect(result -> {
                    ControllerException exception = (ControllerException) result.getResolvedException();
                    assertTrue(exception.getErrorCode() == ErrorCode.INVALID_IMAGE_URL);
                });
    }

    @Test
    @DisplayName("프로필 이미지 삭제 - null URL (실패)")
    void deleteMockProfileImage_nullUrl() throws Exception {
        // when & then
        mockMvc
                .perform(delete(BASE_URL + "/profile/image"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    //이미지 URL 유효성 검증 return false
    @Test
    @DisplayName("프로필 이미지 삭제 - 이미지 URL 유효성 검증 실패")
    void deleteMockProfileImage_invalidImageUrl() throws Exception {
        // given
        String imageUrl = "https://invalid-url.com/profile/1234567890abcdef1234567890abcdef.jpg";

        // when & then
        mockMvc
                .perform(delete(BASE_URL + "/profile/image")
                        .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ControllerException))
                .andExpect(result -> {
                    ControllerException exception = (ControllerException) result.getResolvedException();
                    assertTrue(exception.getErrorCode() == ErrorCode.INVALID_IMAGE_URL);
                });
    }
}