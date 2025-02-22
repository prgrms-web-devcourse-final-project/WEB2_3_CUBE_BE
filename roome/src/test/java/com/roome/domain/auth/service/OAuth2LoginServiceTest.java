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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @InjectMocks
    private OAuth2LoginService oauth2LoginService;

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

    private static final class TestData {
        static final String AUTH_CODE = "test_auth_code";
        static final String ACCESS_TOKEN = "test_access_token";
        static final String EMAIL = "test@example.com";
        static final String NAME = "Test User";
        static final String PROFILE_IMAGE = "test_image_url";
        static final String PROVIDER_ID = "test_provider_id";
    }

    @Nested
    @DisplayName("로그인 성공")
    class SuccessCase {
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

            ResponseEntity<Map> tokenResponse = new ResponseEntity<>(
                    Map.of("access_token", TestData.ACCESS_TOKEN),
                    HttpStatus.OK
            );
            given(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).willReturn(tokenResponse);

            ResponseEntity<Map> userInfoResponse = new ResponseEntity<>(
                    Map.of(
                            "id", TestData.PROVIDER_ID,
                            "kakao_account", Map.of(
                                    "email", TestData.EMAIL,
                                    "profile", Map.of(
                                            "nickname", TestData.NAME,
                                            "profile_image_url", TestData.PROFILE_IMAGE
                                    )
                            )
                    ),
                    HttpStatus.OK
            );
            given(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).willReturn(userInfoResponse);
        }

        @Test
        @DisplayName("신규 유저는 첫 로그인 시 계정과 방이 생성된다")
        void createNewUserAndRoomForFirstLogin() {
            // given
            User newUser = createUser(1L);
            JwtToken jwtToken = createJwtToken();
            RoomResponseDto roomResponse = createRoomResponse(1L);

            given(userRepository.findByEmail(anyString()))
                    .willReturn(Optional.empty());
            given(userRepository.save(any(User.class)))
                    .willReturn(newUser);
            given(jwtTokenProvider.createToken(any(Authentication.class)))
                    .willReturn(jwtToken);
            given(roomService.getRoomByUserId(anyLong()))
                    .willReturn(roomResponse);

            // when
            LoginResponse response = oauth2LoginService.login(OAuth2Provider.KAKAO, TestData.AUTH_CODE);

            // then
            assertLoginResponse(response, newUser, jwtToken);
            verify(userRepository).save(any(User.class));
            verify(roomService).createRoom(newUser.getId());
        }

        @Test
        @DisplayName("기존 유저는 로그인 시 마지막 로그인 시간만 갱신된다")
        void updateLastLoginTimeForExistingUser() {
            // given
            User existingUser = createUser(1L);
            existingUser.updateLastLogin();
            JwtToken jwtToken = createJwtToken();
            RoomResponseDto roomResponse = createRoomResponse(1L);

            given(userRepository.findByEmail(anyString()))
                    .willReturn(Optional.of(existingUser));
            given(jwtTokenProvider.createToken(any(Authentication.class)))
                    .willReturn(jwtToken);
            given(roomService.getRoomByUserId(anyLong()))
                    .willReturn(roomResponse);

            // when
            LoginResponse response = oauth2LoginService.login(OAuth2Provider.KAKAO, TestData.AUTH_CODE);

            // then
            assertLoginResponse(response, existingUser, jwtToken);
            verify(userRepository, never()).save(any(User.class));
            verify(roomService, never()).createRoom(anyLong());
        }
    }

    @Nested
    @DisplayName("로그인 실패")
    class FailureCase {
        @BeforeEach
        void setUp() {
            OAuth2ClientProperties.Registration registration = mock(OAuth2ClientProperties.Registration.class);
            given(oauth2ClientProperties.getRegistration())
                    .willReturn(Map.of("kakao", registration));
            given(registration.getClientId()).willReturn("test-client-id");
            given(registration.getClientSecret()).willReturn("test-client-secret");
            given(registration.getRedirectUri()).willReturn("test-redirect-uri");
        }

        @Test
        @DisplayName("OAuth2 인증 실패 시 예외가 발생한다")
        void throwExceptionForFailedOAuth2Authentication() {
            // given
            given(restTemplate.postForEntity(
                    anyString(),
                    any(HttpEntity.class),
                    eq(Map.class)
            )).willThrow(new RestClientException("OAuth2 authentication failed"));

            // when & then
            assertThrows(OAuth2AuthenticationProcessingException.class, () ->
                    oauth2LoginService.login(OAuth2Provider.KAKAO, TestData.AUTH_CODE)
            );
        }
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .name(TestData.NAME)
                .nickname(TestData.NAME)
                .email(TestData.EMAIL)
                .profileImage(TestData.PROFILE_IMAGE)
                .provider(Provider.KAKAO)
                .providerId(TestData.PROVIDER_ID)
                .status(Status.OFFLINE)
                .build();
    }

    private JwtToken createJwtToken() {
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(TestData.ACCESS_TOKEN)
                .refreshToken("test_refresh_token")
                .build();
    }

    private RoomResponseDto createRoomResponse(Long id) {
        return RoomResponseDto.builder()
                .roomId(id)
                .userId(id)
                .theme("Default Theme")
                .createdAt(LocalDateTime.now())
                .furnitures(Collections.emptyList())
                .build();
    }

    private void assertLoginResponse(LoginResponse response, User user, JwtToken jwtToken) {
        assertNotNull(response);
        assertEquals(jwtToken.getAccessToken(), response.getAccessToken());
        assertEquals(jwtToken.getRefreshToken(), response.getRefreshToken());
        assertEquals(user.getId(), response.getUser().getUserId());
        assertEquals(user.getNickname(), response.getUser().getNickname());
        assertEquals(user.getEmail(), response.getUser().getEmail());
        assertEquals(user.getProfileImage(), response.getUser().getProfileImage());
    }
}