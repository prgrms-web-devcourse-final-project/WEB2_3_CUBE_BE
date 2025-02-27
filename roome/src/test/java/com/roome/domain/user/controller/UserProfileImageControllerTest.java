package com.roome.domain.user.controller;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.service.UserProfileImageService;
import com.roome.global.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserProfileImageControllerTest {

    private final Long USER_ID = 1L;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private S3Service s3Service;
    @MockBean
    private UserProfileImageService userService;
    private OAuth2UserPrincipal mockPrincipal;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        // OAuth2UserPrincipal 직접 모킹
        mockPrincipal = mock(OAuth2UserPrincipal.class);
        when(mockPrincipal.getId()).thenReturn(USER_ID);

        // 직접 인증 객체 생성
        auth = new UsernamePasswordAuthenticationToken(mockPrincipal, null, Collections.emptyList());

        // S3Service 설정
        when(s3Service.getBucketName()).thenReturn("roome-bucket");
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 테스트")
    void uploadProfileImageSuccessTest() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String uploadedImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";
        when(s3Service.uploadImage(any(), eq("profile"))).thenReturn(uploadedImageUrl);

        // when & then
        mockMvc.perform(multipart("/api/users/image")
                        .file(imageFile)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(uploadedImageUrl))
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"));

        verify(userService).updateProfileImage(eq(USER_ID), eq(uploadedImageUrl));
    }

    @Test
    @DisplayName("이미지 파일이 없는 경우 테스트")
    void uploadEmptyImageTest() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        // when & then
        mockMvc.perform(multipart("/api/users/image")
                        .file(emptyFile)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
        verify(userService, never()).updateProfileImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("이미지 파일이 null인 경우 테스트")
    void uploadNullImageTest() throws Exception {
        // 파일 없이 요청
        mockMvc.perform(multipart("/api/users/image")
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
        verify(userService, never()).updateProfileImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("확장자가 없는 파일 업로드 테스트")
    void uploadFileWithoutExtensionTest() throws Exception {
        // given
        MockMultipartFile noExtensionFile = new MockMultipartFile(
                "image",
                "noextension",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/users/image")
                        .file(noExtensionFile)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
    }

    @Test
    @DisplayName("지원하지 않는 이미지 형식 업로드 테스트")
    void uploadUnsupportedImageFormatTest() throws Exception {
        // given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test-document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test pdf content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/users/image")
                        .file(invalidFile)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
        verify(userService, never()).updateProfileImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("너무 큰 이미지 파일 업로드 테스트")
    void uploadTooLargeImageTest() throws Exception {
        // given
        // 5MB를 초과하는 큰 파일 모의
        byte[] largeContent = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile largeFile = new MockMultipartFile(
                "image",
                "large-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent
        );

        // when & then
        mockMvc.perform(multipart("/api/users/image")
                        .file(largeFile)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
    }

    @Test
    @DisplayName("프로필 이미지 삭제 성공 테스트")
    void deleteProfileImageSuccessTest() throws Exception {
        // given
        String imageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";

        // when & then
        mockMvc.perform(delete("/api/users/image")
                        .param("imageUrl", imageUrl)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(imageUrl));

        verify(s3Service).deleteImage(eq(imageUrl));
        verify(userService).deleteProfileImage(eq(USER_ID));
    }

    @Test
    @DisplayName("null 이미지 URL로 삭제 시도 테스트")
    void deleteNullImageUrlTest() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/users/image")
                        .param("imageUrl", (String)null)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).deleteImage(anyString());
        verify(userService, never()).deleteProfileImage(anyLong());
    }

    @Test
    @DisplayName("빈 이미지 URL로 삭제 시도 테스트")
    void deleteEmptyImageUrlTest() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/users/image")
                        .param("imageUrl", "")
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).deleteImage(anyString());
        verify(userService, never()).deleteProfileImage(anyLong());
    }

    @Test
    @DisplayName("잘못된 이미지 URL로 삭제 시도 테스트")
    void deleteInvalidImageUrlTest() throws Exception {
        // given
        String invalidImageUrl = "https://invalid-domain.com/image.jpg";

        // when & then
        mockMvc.perform(delete("/api/users/image")
                        .param("imageUrl", invalidImageUrl)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).deleteImage(anyString());
        verify(userService, never()).deleteProfileImage(anyLong());
    }

    @Test
    @DisplayName("S3 업로드 실패 테스트")
    void s3UploadFailureTest() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(s3Service.uploadImage(any(), eq("profile"))).thenThrow(new IOException("S3 upload failed"));

        // when & then
        mockMvc.perform(multipart("/api/users/image")
                        .file(imageFile)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}