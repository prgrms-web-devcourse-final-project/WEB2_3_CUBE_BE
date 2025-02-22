package com.roome.domain.auth.service;

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
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private ClientRegistration clientRegistration;

    private static final class TestUsers {
        static final String PROVIDER_ID = "12345";
        static final String NAME = "Test User";
        static final String PROFILE_IMAGE = "http://test.com/image.jpg";
        static final String EMAIL = "test@example.com";
    }

    private static final class KakaoAttributes {
        static final Map<String, Object> VALID = Map.of(
                "id", TestUsers.PROVIDER_ID,
                "kakao_account", Map.of(
                        "email", TestUsers.EMAIL,
                        "profile", Map.of(
                                "nickname", TestUsers.NAME,
                                "profile_image_url", TestUsers.PROFILE_IMAGE
                        )
                )
        );
    }

    @BeforeEach
    void setUp() {
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("kakao");
    }

    @Test
    @DisplayName("신규 유저는 로그인 시 회원가입이 진행된다")
    void signUpNewUserWithOAuth2Login() {
        // given
        User newUser = User.builder()
                .name(TestUsers.NAME)
                .nickname(TestUsers.NAME)
                .email(TestUsers.EMAIL)
                .profileImage(TestUsers.PROFILE_IMAGE)
                .provider(Provider.KAKAO)
                .providerId(TestUsers.PROVIDER_ID)
                .status(Status.OFFLINE)
                .build();

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                KakaoAttributes.VALID,
                "id"
        );

        when(userRepository.findByProviderId(TestUsers.PROVIDER_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

        // then
        assertThat(result).isInstanceOf(OAuth2UserPrincipal.class);
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) result;

        assertThat(principal.getUser())
                .satisfies(user -> {
                    assertThat(user.getName()).isEqualTo(TestUsers.NAME);
                    assertThat(user.getNickname()).isEqualTo(TestUsers.NAME);
                    assertThat(user.getEmail()).isEqualTo(TestUsers.EMAIL);
                    assertThat(user.getProfileImage()).isEqualTo(TestUsers.PROFILE_IMAGE);
                    assertThat(user.getProviderId()).isEqualTo(TestUsers.PROVIDER_ID);
                    assertThat(user.getProvider()).isEqualTo(Provider.KAKAO);
                    assertThat(user.getStatus()).isEqualTo(Status.OFFLINE);
                });

        verify(userRepository).findByProviderId(TestUsers.PROVIDER_ID);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("기존 유저는 로그인 시 마지막 로그인 시간이 갱신된다")
    void updateLastLoginTimeForExistingUser() {
        // given
        LocalDateTime now = LocalDateTime.now();
        User existingUser = User.builder()
                .name(TestUsers.NAME)
                .nickname(TestUsers.NAME)
                .email(TestUsers.EMAIL)
                .profileImage(TestUsers.PROFILE_IMAGE)
                .provider(Provider.KAKAO)
                .providerId(TestUsers.PROVIDER_ID)
                .status(Status.OFFLINE)
                .build();

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                KakaoAttributes.VALID,
                "id"
        );

        when(userRepository.findByProviderId(TestUsers.PROVIDER_ID))
                .thenReturn(Optional.of(existingUser));

        // when
        OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

        // then
        assertThat(result).isInstanceOf(OAuth2UserPrincipal.class);
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) result;

        assertThat(principal.getUser())
                .satisfies(user -> {
                    assertThat(user.getName()).isEqualTo(TestUsers.NAME);
                    assertThat(user.getNickname()).isEqualTo(TestUsers.NAME);
                    assertThat(user.getEmail()).isEqualTo(TestUsers.EMAIL);
                    assertThat(user.getProfileImage()).isEqualTo(TestUsers.PROFILE_IMAGE);
                    assertThat(user.getProviderId()).isEqualTo(TestUsers.PROVIDER_ID);
                    assertThat(user.getLastLogin()).isNotNull();
                    assertThat(user.getLastLogin()).isAfterOrEqualTo(now);
                });

        verify(userRepository).findByProviderId(TestUsers.PROVIDER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("지원하지 않는 제공자로 로그인 시도시 예외가 발생한다")
    void throwExceptionForUnsupportedProvider() {
        // given
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                KakaoAttributes.VALID,
                "id"
        );
        when(clientRegistration.getRegistrationId()).thenReturn("invalid");

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(userRequest, oAuth2User))
                .isInstanceOf(InternalAuthenticationServiceException.class)
                .hasMessage("지원하지 않는 로그인 방식입니다.");

        verify(userRepository, never()).findByProviderId(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("유저 조회 중 DB 오류 발생시 예외가 발생한다")
    void throwExceptionWhenDatabaseError() {
        // given
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                KakaoAttributes.VALID,
                "id"
        );
        when(userRepository.findByProviderId(anyString()))
                .thenThrow(new RuntimeException("DB Error"));

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(userRequest, oAuth2User))
                .isInstanceOf(InternalAuthenticationServiceException.class)
                .hasMessageContaining("DB Error");
    }
}