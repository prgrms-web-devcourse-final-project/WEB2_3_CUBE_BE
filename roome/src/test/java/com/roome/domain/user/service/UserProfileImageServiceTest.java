package com.roome.domain.user.service;

import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileImageServiceTest {

    private final Long userId = 1L;
    private final String profileImageUrl = "https://example.com/image.jpg";
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserProfileImageService userProfileImageService;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .nickname("tester")
                .profileImage(profileImageUrl)
                .provider(Provider.GOOGLE)
                .providerId("google123")
                .status(Status.OFFLINE)
                .build();
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 성공 테스트")
    void updateProfileImage_Success() {
        // given
        String newProfileImageUrl = "https://example.com/new-image.jpg";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // when
        userProfileImageService.updateProfileImage(userId, newProfileImageUrl);

        // then
        assertEquals(newProfileImageUrl, testUser.getProfileImage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 실패 테스트 - 사용자 없음")
    void updateProfileImage_UserNotFound() {
        // given
        String newProfileImageUrl = "https://example.com/new-image.jpg";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> userProfileImageService.updateProfileImage(userId, newProfileImageUrl));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("프로필 이미지 삭제 성공 테스트")
    void deleteProfileImage_Success() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // when
        userProfileImageService.deleteProfileImage(userId);

        // then
        assertNull(testUser.getProfileImage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("프로필 이미지 삭제 실패 테스트 - 사용자 없음")
    void deleteProfileImage_UserNotFound() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> userProfileImageService.deleteProfileImage(userId));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("프로필 이미지 URL 조회 성공 테스트")
    void getProfileImageUrl_Success() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // when
        String result = userProfileImageService.getProfileImageUrl(userId);

        // then
        assertEquals(profileImageUrl, result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("프로필 이미지 URL 조회 실패 테스트 - 사용자 없음")
    void getProfileImageUrl_UserNotFound() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> userProfileImageService.getProfileImageUrl(userId));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findById(userId);
    }
}