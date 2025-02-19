package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.exception.OAuth2AuthenticationProcessingException;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RoomService roomService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private OAuth2ClientProperties oauth2ClientProperties;

    @InjectMocks
    private OAuth2LoginService oauth2LoginService;

    private static final String TEST_AUTH_CODE = "test_auth_code";
    private static final String TEST_ACCESS_TOKEN = "test_access_token";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_PROFILE_IMAGE = "test_image_url";
    private static final String TEST_PROVIDER_ID = "test_provider_id";

    @BeforeEach
    void setUp() {
        OAuth2ClientProperties.Registration registration = mock(OAuth2ClientProperties.Registration.class);
        Map<String, OAuth2ClientProperties.Registration> registrations = Map.of(
                "kakao", registration
        );

        given(oauth2ClientProperties.getRegistration())
                .willReturn(registrations);
        given(registration.getClientId())
                .willReturn("test-client-id");
        given(registration.getClientSecret())
                .willReturn("test-client-secret");
        given(registration.getRedirectUri())
                .willReturn("test-redirect-uri");
    }

    @Test
    @DisplayName("로그인 성공 - 신규 사용자")
    void loginSuccess_NewUser() {
        // given
        User newUser = createUser(1L);
        JwtToken jwtToken = createJwtToken();
        RoomResponseDto roomResponseDto = RoomResponseDto.builder()
                .roomId(1L)
                .userId(1L)
                .theme("Default Theme")
                .createdAt(LocalDateTime.now())
                .furnitures(Collections.emptyList())
                .build();

        mockTokenResponse();
        mockUserProfileResponse();

        given(userRepository.findByProviderId(anyString()))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class)))
                .willReturn(newUser);
        given(jwtTokenProvider.createToken(any(Authentication.class)))
                .willReturn(jwtToken);
        given(roomService.getRoomByUserId(newUser.getId()))
                .willReturn(roomResponseDto);

        // when
        LoginResponse response = oauth2LoginService.login(OAuth2Provider.KAKAO, TEST_AUTH_CODE);

        // then
        assertNotNull(response);
        assertEquals(jwtToken.getAccessToken(), response.getAccessToken());
        assertEquals(jwtToken.getRefreshToken(), response.getRefreshToken());
        assertEquals(newUser.getId(), response.getUser().getUserId());
        verify(userRepository).save(any(User.class));
        verify(roomService).createRoom(newUser.getId());
    }

    @Test
    @DisplayName("로그인 성공 - 기존 사용자")
    void loginSuccess_ExistingUser() {
        // given
        User existingUser = createUser(1L);
        existingUser.updateLastLogin();
        JwtToken jwtToken = createJwtToken();
        RoomResponseDto roomResponseDto = RoomResponseDto.builder()
                .roomId(1L)
                .userId(1L)
                .theme("Default Theme")
                .createdAt(LocalDateTime.now())
                .furnitures(Collections.emptyList())
                .build();

        mockTokenResponse();
        mockUserProfileResponse();

        given(userRepository.findByProviderId(anyString()))
                .willReturn(Optional.of(existingUser));
        given(jwtTokenProvider.createToken(any(Authentication.class)))
                .willReturn(jwtToken);
        given(roomService.getRoomByUserId(existingUser.getId()))
                .willReturn(roomResponseDto);

        // when
        LoginResponse response = oauth2LoginService.login(OAuth2Provider.KAKAO, TEST_AUTH_CODE);

        // then
        assertNotNull(response);
        assertEquals(jwtToken.getAccessToken(), response.getAccessToken());
        assertEquals(jwtToken.getRefreshToken(), response.getRefreshToken());
        assertEquals(existingUser.getId(), response.getUser().getUserId());
        verify(userRepository, never()).save(any(User.class));
        verify(roomService, never()).createRoom(anyLong());
    }

    @Test
    @DisplayName("OAuth2 인증 실패 시 예외가 발생한다")
    void loginFail_OAuth2AuthenticationFailed() {
        // given
        given(restTemplate.postForObject(
                anyString(),
                any(Map.class),
                eq(Map.class)))
                .willThrow(new RestClientException("Invalid auth code"));

        // when & then
        assertThrows(OAuth2AuthenticationProcessingException.class, () ->
                oauth2LoginService.login(OAuth2Provider.KAKAO, TEST_AUTH_CODE)
        );
    }

    private void mockTokenResponse() {
        Map<String, Object> tokenResponse = Map.of("access_token", TEST_ACCESS_TOKEN);
        given(restTemplate.postForObject(
                anyString(),
                any(Map.class),
                eq(Map.class)))
                .willReturn(tokenResponse);
    }

    private void mockUserProfileResponse() {
        Map<String, Object> mockUserInfo = Map.of(
                "id", TEST_PROVIDER_ID,
                "kakao_account", Map.of(
                        "email", TEST_EMAIL,
                        "profile", Map.of(
                                "nickname", TEST_NAME,
                                "profile_image_url", TEST_PROFILE_IMAGE
                        )
                )
        );
        given(restTemplate.getForObject(anyString(), eq(Map.class)))
                .willReturn(mockUserInfo);
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .name(TEST_NAME)
                .nickname(TEST_NAME)
                .email(TEST_EMAIL)
                .profileImage(TEST_PROFILE_IMAGE)
                .provider(Provider.KAKAO)
                .providerId(TEST_PROVIDER_ID)
                .status(Status.OFFLINE)
                .build();
    }

    private JwtToken createJwtToken() {
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(TEST_ACCESS_TOKEN)
                .refreshToken("test_refresh_token")
                .build();
    }
}