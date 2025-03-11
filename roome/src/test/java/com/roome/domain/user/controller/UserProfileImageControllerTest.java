package com.roome.domain.user.controller;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserProfileImageControllerTest {

    private final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private UserProfileImageService userService;

    @BeforeEach
    void setUp() {
        // 인증 설정 - 사용자 ID를 Principal로 사용
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                USER_ID, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // S3Service 설정
        when(s3Service.getBucketName()).thenReturn("roome-bucket");
    }

    // AuthenticatedUser 어노테이션을 사용한 인증된 요청을 수행하는 헬퍼 메서드
    private ResultActions performWithAuthenticatedUser(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        // SecurityContext에 인증 정보가 이미 설정되어 있으므로 바로 수행
        return mockMvc.perform(requestBuilder);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 테스트 (POST)")
    @WithMockUser
    void uploadProfileImageSuccessPostTest() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String uploadedImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";
        when(s3Service.uploadImage(any(), eq("profile"))).thenReturn(uploadedImageUrl);
        when(userService.getProfileImageUrl(USER_ID)).thenReturn(null);

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(imageFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(uploadedImageUrl))
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"));

        verify(userService).updateProfileImage(eq(USER_ID), eq(uploadedImageUrl));
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 테스트 (PUT)")
    @WithMockUser
    void uploadProfileImageSuccessPutTest() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String uploadedImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";
        when(s3Service.uploadImage(any(), eq("profile"))).thenReturn(uploadedImageUrl);
        when(userService.getProfileImageUrl(USER_ID)).thenReturn(null);

        // PUT 요청 생성
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/api/users/image");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        // when & then
        performWithAuthenticatedUser(builder.file(imageFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(uploadedImageUrl))
                .andExpect(jsonPath("$.fileName").value("test-image.jpg"));

        verify(userService).updateProfileImage(eq(USER_ID), eq(uploadedImageUrl));
    }

    @Test
    @DisplayName("기존 프로필 이미지가 있는 경우 업로드 테스트")
    @WithMockUser
    void uploadProfileImageWithExistingImageTest() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String existingImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/existing123456789012345678901234.jpg";
        String newImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/new1234567890123456789012345678901.jpg";

        when(userService.getProfileImageUrl(USER_ID)).thenReturn(existingImageUrl);
        when(s3Service.uploadImage(any(), eq("profile"))).thenReturn(newImageUrl);

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(imageFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(newImageUrl));

        verify(userService).updateProfileImage(eq(USER_ID), eq(newImageUrl));
        verify(s3Service).deleteImage(eq(existingImageUrl));
    }

    @Test
    @DisplayName("이미지 파일이 없는 경우 테스트")
    @WithMockUser
    void uploadEmptyImageTest() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(emptyFile))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
        verify(userService, never()).updateProfileImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("이미지 파일이 null인 경우 테스트")
    @WithMockUser
    void uploadNullImageTest() throws Exception {
        // 파일 없이 요청
        performWithAuthenticatedUser(multipart("/api/users/image"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
        verify(userService, never()).updateProfileImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("확장자가 없는 파일 업로드 테스트")
    @WithMockUser
    void uploadFileWithoutExtensionTest() throws Exception {
        // given
        MockMultipartFile noExtensionFile = new MockMultipartFile(
                "image",
                "noextension",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(noExtensionFile))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
    }

    @Test
    @DisplayName("지원하지 않는 이미지 형식 업로드 테스트")
    @WithMockUser
    void uploadUnsupportedImageFormatTest() throws Exception {
        // given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test-document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "test pdf content".getBytes()
        );

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(invalidFile))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
        verify(userService, never()).updateProfileImage(anyLong(), anyString());
    }

    @Test
    @DisplayName("너무 큰 이미지 파일 업로드 테스트")
    @WithMockUser
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
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(largeFile))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).uploadImage(any(), anyString());
    }

    @Test
    @DisplayName("프로필 이미지 삭제 성공 테스트")
    @WithMockUser
    void deleteProfileImageSuccessTest() throws Exception {
        // given
        String imageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";

        // when & then
        performWithAuthenticatedUser(delete("/api/users/image")
                .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(imageUrl));

        verify(s3Service).deleteImage(eq(imageUrl));
        verify(userService).deleteProfileImage(eq(USER_ID));
    }

    @Test
    @DisplayName("null 이미지 URL로 삭제 시도 테스트")
    @WithMockUser
    void deleteNullImageUrlTest() throws Exception {
        // when & then
        performWithAuthenticatedUser(delete("/api/users/image")
                .param("imageUrl", (String)null))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).deleteImage(anyString());
        verify(userService, never()).deleteProfileImage(anyLong());
    }

    @Test
    @DisplayName("빈 이미지 URL로 삭제 시도 테스트")
    @WithMockUser
    void deleteEmptyImageUrlTest() throws Exception {
        // when & then
        performWithAuthenticatedUser(delete("/api/users/image")
                .param("imageUrl", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).deleteImage(anyString());
        verify(userService, never()).deleteProfileImage(anyLong());
    }

    @Test
    @DisplayName("잘못된 이미지 URL로 삭제 시도 테스트")
    @WithMockUser
    void deleteInvalidImageUrlTest() throws Exception {
        // given
        String invalidImageUrl = "https://invalid-domain.com/image.jpg";

        // when & then
        performWithAuthenticatedUser(delete("/api/users/image")
                .param("imageUrl", invalidImageUrl))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).deleteImage(anyString());
        verify(userService, never()).deleteProfileImage(anyLong());
    }

    @Test
    @DisplayName("S3 업로드 실패 테스트")
    @WithMockUser
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
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(imageFile))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("S3 삭제 실패 테스트")
    @WithMockUser
    void s3DeleteFailureTest() throws Exception {
        // given
        String imageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";
        doThrow(new RuntimeException("S3 delete failed")).when(s3Service).deleteImage(anyString());

        // when & then
        performWithAuthenticatedUser(delete("/api/users/image")
                .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(s3Service).deleteImage(eq(imageUrl));
        verify(userService, never()).deleteProfileImage(anyLong());
    }

    @Test
    @DisplayName("지원되는 다른 이미지 형식(PNG) 업로드 테스트")
    @WithMockUser
    void uploadPngImageFormatTest() throws Exception {
        // given
        MockMultipartFile pngFile = new MockMultipartFile(
                "image",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "test png content".getBytes()
        );

        String uploadedImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.png";
        when(s3Service.uploadImage(any(), eq("profile"))).thenReturn(uploadedImageUrl);

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(pngFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(uploadedImageUrl));

        verify(userService).updateProfileImage(eq(USER_ID), eq(uploadedImageUrl));
    }

    @Test
    @DisplayName("지원되는 다른 이미지 형식(GIF) 업로드 테스트")
    @WithMockUser
    void uploadGifImageFormatTest() throws Exception {
        // given
        MockMultipartFile gifFile = new MockMultipartFile(
                "image",
                "test-image.gif",
                "image/gif",
                "test gif content".getBytes()
        );

        String uploadedImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.gif";
        when(s3Service.uploadImage(any(), eq("profile"))).thenReturn(uploadedImageUrl);

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(gifFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(uploadedImageUrl));

        verify(userService).updateProfileImage(eq(USER_ID), eq(uploadedImageUrl));
    }

    @Test
    @DisplayName("빈 파일명이지만 확장자가 있는 경우 테스트")
    @WithMockUser
    void uploadEmptyFilenameWithExtensionTest() throws Exception {
        // given
        MockMultipartFile fileWithExtension = new MockMultipartFile(
                "image",
                ".jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String uploadedImageUrl = "https://roome-bucket.s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";
        when(s3Service.uploadImage(any(), eq("profile"))).thenReturn(uploadedImageUrl);

        // when & then
        performWithAuthenticatedUser(multipart("/api/users/image")
                .file(fileWithExtension))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).updateProfileImage(eq(USER_ID), eq(uploadedImageUrl));
    }

    @Test
    @DisplayName("URL에 버킷 이름만 없는 경우 테스트")
    @WithMockUser
    void deleteImageUrlWithoutBucketNameTest() throws Exception {
        // given - 버킷 이름이 없지만 나머지 패턴은 맞는 URL
        String imageUrl = "https://s3.amazonaws.com/profile/12345abcde12345abcde12345abcde12.jpg";

        // when & then
        performWithAuthenticatedUser(delete("/api/users/image")
                .param("imageUrl", imageUrl))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(s3Service, never()).deleteImage(anyString());
    }
}