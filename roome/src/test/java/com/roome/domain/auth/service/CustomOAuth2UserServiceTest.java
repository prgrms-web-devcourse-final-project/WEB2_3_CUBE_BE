package com.roome.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

  @InjectMocks
  private CustomOAuth2UserService customOAuth2UserService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoomService roomService;

  @Mock
  private PointHistoryRepository pointHistoryRepository;

  private static final class TestUsers {

    static final String PROVIDER_ID = "12345";
    static final String NAME = "Test User";
    static final String PROFILE_IMAGE = "http://test.com/image.jpg";
    static final String EMAIL = "test@example.com";
  }

  private static final class KakaoAttributes {

    static final Map<String, Object> VALID = Map.of("id", TestUsers.PROVIDER_ID, "kakao_account",
        Map.of("email", TestUsers.EMAIL, "profile",
            Map.of("nickname", TestUsers.NAME, "profile_image_url", TestUsers.PROFILE_IMAGE)));
  }

  @Test
  @DisplayName("신규 유저는 로그인 시 회원가입이 진행된다")
  void signUpNewUserWithOAuth2Login() {
    // given
    User newUser = User.builder().id(1L).name(TestUsers.NAME).nickname(TestUsers.NAME)
        .email(TestUsers.EMAIL).profileImage(TestUsers.PROFILE_IMAGE).provider(Provider.KAKAO)
        .providerId(TestUsers.PROVIDER_ID).status(Status.OFFLINE).point(new Point(null, 0, 0, 0))
        .build();

    OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), KakaoAttributes.VALID,
        "id");

    OAuth2UserRequest userRequest = createOAuth2UserRequest("KAKAO");

    // ClientRegistration을 직접 모킹
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    when(mockClientRegistration.getRegistrationId()).thenReturn("KAKAO");
    when(userRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    try (MockedStatic<OAuth2Factory> mockedStatic = mockStatic(OAuth2Factory.class)) {
      // OAuth2Factory 모킹
      OAuth2Response oAuth2Response = mock(OAuth2Response.class);
      when(oAuth2Response.getProviderId()).thenReturn(TestUsers.PROVIDER_ID);
      when(oAuth2Response.getEmail()).thenReturn(TestUsers.EMAIL);
      when(oAuth2Response.getName()).thenReturn(TestUsers.NAME);
      when(oAuth2Response.getProfileImageUrl()).thenReturn(TestUsers.PROFILE_IMAGE);
      when(oAuth2Response.getProvider()).thenReturn(OAuth2Provider.KAKAO);

      mockedStatic.when(() -> OAuth2Factory.createResponse(any(), any()))
          .thenReturn(oAuth2Response);

      when(userRepository.findByEmail(TestUsers.EMAIL)).thenReturn(Optional.empty());
      when(userRepository.save(any(User.class))).thenReturn(newUser);

      // when
      OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

      // then
      assertThat(result).isInstanceOf(OAuth2UserPrincipal.class);
      OAuth2UserPrincipal principal = (OAuth2UserPrincipal) result;

      assertThat(principal.getUser()).satisfies(user -> {
        assertThat(user.getName()).isEqualTo(TestUsers.NAME);
        assertThat(user.getNickname()).isEqualTo(TestUsers.NAME);
        assertThat(user.getEmail()).isEqualTo(TestUsers.EMAIL);
        assertThat(user.getProfileImage()).isEqualTo(TestUsers.PROFILE_IMAGE);
        assertThat(user.getProviderId()).isEqualTo(TestUsers.PROVIDER_ID);
        assertThat(user.getProvider()).isEqualTo(Provider.KAKAO);
        assertThat(user.getStatus()).isEqualTo(Status.OFFLINE);
        assertThat(user.getPoint()).isNotNull();
      });

      verify(userRepository).findByEmail(TestUsers.EMAIL);
      verify(userRepository, times(2)).save(any(User.class));

      // roomService
      // verify(roomService).getOrCreateRoomByUserId(anyLong());

      // accumulateAttendancePoints가 주석 처리
      // verify(pointHistoryRepository, atLeastOnce()).save(any());
    }
  }


  @Test
  @DisplayName("기존 유저는 로그인 시 마지막 로그인 시간이 갱신된다")
  void updateLastLoginTimeForExistingUser() {
    // given
    LocalDateTime now = LocalDateTime.now();
    User existingUser = User.builder().id(1L).name(TestUsers.NAME).nickname(TestUsers.NAME)
        .email(TestUsers.EMAIL).profileImage(TestUsers.PROFILE_IMAGE).provider(Provider.KAKAO)
        .providerId(TestUsers.PROVIDER_ID).status(Status.OFFLINE).lastLogin(now.minusDays(1))
        .point(new Point(null, 0, 0, 0)).build();

    OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), KakaoAttributes.VALID,
        "id");

    OAuth2UserRequest userRequest = createOAuth2UserRequest("KAKAO");
    // ClientRegistration을 직접 모킹
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    when(mockClientRegistration.getRegistrationId()).thenReturn("KAKAO");
    lenient().when(userRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    try (MockedStatic<OAuth2Factory> mockedStatic = mockStatic(OAuth2Factory.class)) {
      // OAuth2Factory 모킹
      OAuth2Response oAuth2Response = mock(OAuth2Response.class);
      when(oAuth2Response.getProviderId()).thenReturn(TestUsers.PROVIDER_ID);
      when(oAuth2Response.getEmail()).thenReturn(TestUsers.EMAIL);
      when(oAuth2Response.getName()).thenReturn(TestUsers.NAME);
      when(oAuth2Response.getProfileImageUrl()).thenReturn(TestUsers.PROFILE_IMAGE);
      when(oAuth2Response.getProvider()).thenReturn(OAuth2Provider.KAKAO);

      mockedStatic.when(() -> OAuth2Factory.createResponse(any(), any()))
          .thenReturn(oAuth2Response);

      when(userRepository.findByEmail(TestUsers.EMAIL)).thenReturn(Optional.of(existingUser));

      // when
      OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

      // then
      assertThat(result).isInstanceOf(OAuth2UserPrincipal.class);
      OAuth2UserPrincipal principal = (OAuth2UserPrincipal) result;

      assertThat(principal.getUser()).satisfies(user -> {
        assertThat(user.getName()).isEqualTo(TestUsers.NAME);
        assertThat(user.getNickname()).isEqualTo(TestUsers.NAME);
        assertThat(user.getEmail()).isEqualTo(TestUsers.EMAIL);
        assertThat(user.getProfileImage()).isEqualTo(TestUsers.PROFILE_IMAGE);
        assertThat(user.getProviderId()).isEqualTo(TestUsers.PROVIDER_ID);
        assertThat(user.getLastLogin()).isNotNull();
      });

      verify(userRepository).findByEmail(TestUsers.EMAIL);
      verify(userRepository).save(any(User.class));
      verify(roomService, never()).getOrCreateRoomByUserId(anyLong());
    }
  }

  @Test
  @DisplayName("지원하지 않는 제공자로 로그인 시도시 예외가 발생한다")
  void throwExceptionForUnsupportedProvider() {
    // given
    OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), KakaoAttributes.VALID,
        "id");

    OAuth2UserRequest userRequest = createOAuth2UserRequest("INVALID");
    // ClientRegistration을 직접 모킹
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    when(mockClientRegistration.getRegistrationId()).thenReturn("INVALID");
    when(userRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    // when & then
    assertThatThrownBy(
        () -> customOAuth2UserService.processOAuth2User(userRequest, oAuth2User)).isInstanceOf(
        OAuth2AuthenticationException.class).hasMessageContaining("OAuth2 인증 실패");

    verify(userRepository, never()).findByEmail(anyString());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("유저 조회 중 DB 오류 발생시 예외가 발생한다")
  void throwExceptionWhenDatabaseError() {
    // given
    OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), KakaoAttributes.VALID,
        "id");

    OAuth2UserRequest userRequest = createOAuth2UserRequest("KAKAO");
    // ClientRegistration을 직접 모킹
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    when(mockClientRegistration.getRegistrationId()).thenReturn("KAKAO");
    when(userRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    try (MockedStatic<OAuth2Factory> mockedStatic = mockStatic(OAuth2Factory.class)) {
      // OAuth2Factory 모킹
      OAuth2Response oAuth2Response = mock(OAuth2Response.class);
      // 테스트에 필요한 정보만 모킹
      when(oAuth2Response.getEmail()).thenReturn(TestUsers.EMAIL);

      mockedStatic.when(() -> OAuth2Factory.createResponse(any(), any()))
          .thenReturn(oAuth2Response);

      when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB Error"));

      // when & then
      assertThatThrownBy(
          () -> customOAuth2UserService.processOAuth2User(userRequest, oAuth2User)).isInstanceOf(
          OAuth2AuthenticationException.class).hasMessageContaining("OAuth2 인증 실패");
    }
  }

  private OAuth2UserRequest createOAuth2UserRequest(String registrationId) {
    // 실제 ClientRegistration 객체를 생성하지 않고 Mock으로 대체
    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);

    return userRequest;
  }
}