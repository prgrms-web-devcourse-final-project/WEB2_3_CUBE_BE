package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private OAuth2User oAuth2User;

    private OAuth2Response mockOAuth2Response;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockOAuth2Response = mock(OAuth2Response.class);
        when(mockOAuth2Response.getProviderId()).thenReturn("123456");
        when(mockOAuth2Response.getName()).thenReturn("Test User");
        when(mockOAuth2Response.getProfileImageUrl()).thenReturn("test-image.jpg");
        when(mockOAuth2Response.getProvider()).thenReturn(OAuth2Provider.KAKAO);

        mockUser = User.builder()
                .id(1L)
                .name("Test User")
                .nickname("testNick")
                .profileImage("test-image.jpg")
                .provider(Provider.KAKAO)
                .providerId("123456")
                .status(Status.ONLINE)
                .build();
    }

    @Test
    @DisplayName("OAuth2 로그인 성공")
    void loadUser_Success() {
        // Given
        when(oAuth2User.getAttributes()).thenReturn(Map.of("id", "123456"));
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("kakao");
        when(userRepository.findByProviderId("123456")).thenReturn(Optional.of(mockUser));

        // When
        OAuth2UserPrincipal userPrincipal = (OAuth2UserPrincipal) customOAuth2UserService.loadUser(userRequest);

        // Then
        assertThat(userPrincipal).isNotNull();
        assertThat(userPrincipal.getUser().getProviderId()).isEqualTo("123456");
        verify(userRepository, times(1)).findByProviderId("123456");
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 예외 발생")
    void loadUser_Exception() {
        // Given
        when(oAuth2User.getAttributes()).thenReturn(Map.of());
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("kakao");

        // When & Then
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
