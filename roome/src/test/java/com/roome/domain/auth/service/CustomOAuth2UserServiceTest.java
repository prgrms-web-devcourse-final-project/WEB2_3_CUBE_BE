package com.roome.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.point.service.PointService;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

  // 테스트 데이터 상수 정의
  private static final String TEST_NAME = "Test User";
  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_PROFILE_IMAGE = "http://example.com/profile.jpg";
  private static final String TEST_PROVIDER_ID = "12345";

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoomRepository roomRepository;

  @Mock
  private FurnitureRepository furnitureRepository;

  @Mock
  private PointRepository pointRepository;

  @Mock
  private PointHistoryRepository pointHistoryRepository;

  @Mock
  private RoomService roomService;

  @Mock
  private PointService pointService;

  @Mock
  private UserActivityService userActivityService;

  @InjectMocks
  private CustomOAuth2UserService customOAuth2UserService;

  private OAuth2UserRequest createOAuth2UserRequest(String registrationId) {
    return mock(OAuth2UserRequest.class);
  }

  @Test
  @DisplayName("기존 유저는 로그인 시 마지막 로그인 시간이 갱신된다")
  void updateLastLoginTimeForExistingUser() {
    // given
    LocalDateTime now = LocalDateTime.now();
    User existingUser = User.builder()
        .id(1L)
        .name(TEST_NAME)
        .nickname(TEST_NAME)
        .email(TEST_EMAIL)
        .profileImage(TEST_PROFILE_IMAGE)
        .provider(Provider.KAKAO)
        .providerId(TEST_PROVIDER_ID)
        .status(Status.OFFLINE)
        .lastLogin(now.minusDays(1))
        .point(new Point(null, 0, 0, 0))
        .build();

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", TEST_PROVIDER_ID);
    Map<String, Object> properties = new HashMap<>();
    properties.put("nickname", TEST_NAME);
    properties.put("profile_image", TEST_PROFILE_IMAGE);
    attributes.put("properties", properties);
    Map<String, Object> kakaoAccount = new HashMap<>();
    kakaoAccount.put("email", TEST_EMAIL);
    attributes.put("kakao_account", kakaoAccount);

    OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "id");

    OAuth2UserRequest userRequest = createOAuth2UserRequest("KAKAO");
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    lenient().when(mockClientRegistration.getRegistrationId()).thenReturn("KAKAO");
    lenient().when(userRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    // UserActivityService 모킹
    when(userActivityService.recordUserActivity(anyLong(), any(ActivityType.class),
        any())).thenReturn(true);

    try (MockedStatic<OAuth2Factory> mockedStatic = mockStatic(OAuth2Factory.class)) {
      // OAuth2Factory 모킹
      OAuth2Response oAuth2Response = mock(OAuth2Response.class);
      lenient().when(oAuth2Response.getProviderId()).thenReturn(TEST_PROVIDER_ID);
      lenient().when(oAuth2Response.getEmail()).thenReturn(TEST_EMAIL);
      lenient().when(oAuth2Response.getName()).thenReturn(TEST_NAME);
      lenient().when(oAuth2Response.getProfileImageUrl()).thenReturn(TEST_PROFILE_IMAGE);
      lenient().when(oAuth2Response.getProvider()).thenReturn(OAuth2Provider.KAKAO);

      mockedStatic.when(() -> OAuth2Factory.createResponse(any(), any()))
          .thenReturn(oAuth2Response);

      when(userRepository.findByProviderAndProviderId(Provider.KAKAO, TEST_PROVIDER_ID))
          .thenReturn(Optional.of(existingUser));

      // when
      OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

      // then
      assertThat(result).isInstanceOf(OAuth2UserPrincipal.class);
      OAuth2UserPrincipal principal = (OAuth2UserPrincipal) result;

      assertThat(principal.getUser()).satisfies(user -> {
        assertThat(user.getName()).isEqualTo(TEST_NAME);
        assertThat(user.getNickname()).isEqualTo(TEST_NAME);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getProfileImage()).isEqualTo(TEST_PROFILE_IMAGE);
        assertThat(user.getProviderId()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(user.getLastLogin()).isNotNull();
      });

      verify(userRepository).findByProviderAndProviderId(Provider.KAKAO, TEST_PROVIDER_ID);
      verify(roomService, never()).getOrCreateRoomByUserId(anyLong());

      // 출석 체크 기록 확인
      verify(userActivityService).recordUserActivity(eq(1L), eq(ActivityType.ATTENDANCE), isNull());
    }
  }

  @Test
  @DisplayName("신규 유저는 로그인 시 회원가입이 진행된다")
  void signUpNewUserWithOAuth2Login() {
    // given
    User newUser = User.builder()
        .id(1L)
        .name(TEST_NAME)
        .nickname(TEST_NAME)
        .email(TEST_EMAIL)
        .profileImage(TEST_PROFILE_IMAGE)
        .provider(Provider.KAKAO)
        .providerId(TEST_PROVIDER_ID)
        .status(Status.OFFLINE)
        .point(new Point(null, 0, 0, 0))
        .build();

    // 직접 속성 맵 생성
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("id", TEST_PROVIDER_ID);
    Map<String, Object> properties = new HashMap<>();
    properties.put("nickname", TEST_NAME);
    properties.put("profile_image", TEST_PROFILE_IMAGE);
    attributes.put("properties", properties);
    Map<String, Object> kakaoAccount = new HashMap<>();
    kakaoAccount.put("email", TEST_EMAIL);
    attributes.put("kakao_account", kakaoAccount);

    OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "id");

    OAuth2UserRequest userRequest = createOAuth2UserRequest("KAKAO");

    // ClientRegistration을 직접 모킹
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    lenient().when(mockClientRegistration.getRegistrationId()).thenReturn("KAKAO");
    lenient().when(userRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    // 신규 유저 생성에 필요한 추가 모킹
    Point mockPoint = new Point(newUser, 0, 0, 0);
    Room mockRoom = Room.builder().id(1L).user(newUser).theme(RoomTheme.BASIC).build();
    List<Furniture> mockFurnitures = new ArrayList<>();

    when(pointRepository.saveAndFlush(any(Point.class))).thenReturn(mockPoint);
    when(roomRepository.saveAndFlush(any(Room.class))).thenReturn(mockRoom);
    when(furnitureRepository.saveAllAndFlush(anyList())).thenReturn(mockFurnitures);
    when(userActivityService.recordUserActivity(anyLong(), any(ActivityType.class),
        any())).thenReturn(true);

    try (MockedStatic<OAuth2Factory> mockedStatic = mockStatic(OAuth2Factory.class)) {
      // OAuth2Factory 모킹
      OAuth2Response oAuth2Response = mock(OAuth2Response.class);
      lenient().when(oAuth2Response.getProviderId()).thenReturn(TEST_PROVIDER_ID);
      lenient().when(oAuth2Response.getEmail()).thenReturn(TEST_EMAIL);
      lenient().when(oAuth2Response.getName()).thenReturn(TEST_NAME);
      lenient().when(oAuth2Response.getProfileImageUrl()).thenReturn(TEST_PROFILE_IMAGE);
      lenient().when(oAuth2Response.getProvider()).thenReturn(OAuth2Provider.KAKAO);

      mockedStatic.when(() -> OAuth2Factory.createResponse(any(), any()))
          .thenReturn(oAuth2Response);

      when(userRepository.findByProviderAndProviderId(Provider.KAKAO, TEST_PROVIDER_ID))
          .thenReturn(Optional.empty());
      when(userRepository.saveAndFlush(any(User.class))).thenReturn(newUser);
      when(userRepository.findById(anyLong())).thenReturn(Optional.of(newUser));

      // when
      OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

      // then
      assertThat(result).isInstanceOf(OAuth2UserPrincipal.class);
      OAuth2UserPrincipal principal = (OAuth2UserPrincipal) result;

      assertThat(principal.getUser()).satisfies(user -> {
        assertThat(user.getName()).isEqualTo(TEST_NAME);
        assertThat(user.getNickname()).isEqualTo(TEST_NAME);
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getProfileImage()).isEqualTo(TEST_PROFILE_IMAGE);
        assertThat(user.getProviderId()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(user.getProvider()).isEqualTo(Provider.KAKAO);
        assertThat(user.getStatus()).isEqualTo(Status.OFFLINE);
        assertThat(user.getPoint()).isNotNull();
      });

      verify(userRepository).findByProviderAndProviderId(Provider.KAKAO, TEST_PROVIDER_ID);
      verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
      verify(pointRepository).saveAndFlush(any(Point.class));
      verify(roomRepository).saveAndFlush(any(Room.class));
      verify(furnitureRepository).saveAllAndFlush(anyList());
      verify(userActivityService).recordUserActivity(anyLong(), eq(ActivityType.ATTENDANCE),
          isNull());
    }
  }

  @Test
  @DisplayName("같은 이메일이지만 다른 제공자로 로그인시 새 계정이 생성된다")
  void createNewUserWhenSameEmailDifferentProvider() {
    // given
    String googleProviderId = "google-" + TEST_PROVIDER_ID;

    User existingUser = User.builder()
        .id(1L)
        .name(TEST_NAME)
        .nickname(TEST_NAME)
        .email(TEST_EMAIL)
        .profileImage(TEST_PROFILE_IMAGE)
        .provider(Provider.KAKAO)
        .providerId(TEST_PROVIDER_ID)
        .status(Status.OFFLINE)
        .lastLogin(LocalDateTime.now().minusDays(1))
        .point(new Point(null, 0, 0, 0))
        .build();

    User newUser = User.builder()
        .id(2L)
        .name(TEST_NAME)
        .nickname(TEST_NAME)
        .email(TEST_EMAIL)
        .profileImage(TEST_PROFILE_IMAGE)
        .provider(Provider.GOOGLE)
        .providerId(googleProviderId)
        .status(Status.OFFLINE)
        .point(new Point(null, 0, 0, 0))
        .build();

    // 구글 속성 직접 생성
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", googleProviderId);
    attributes.put("email", TEST_EMAIL);
    attributes.put("name", TEST_NAME);
    attributes.put("picture", TEST_PROFILE_IMAGE);

    OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");

    OAuth2UserRequest userRequest = createOAuth2UserRequest("GOOGLE");
    ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
    lenient().when(mockClientRegistration.getRegistrationId()).thenReturn("GOOGLE");
    lenient().when(userRequest.getClientRegistration()).thenReturn(mockClientRegistration);

    // 신규 유저 생성에 필요한 추가 모킹
    Point mockPoint = new Point(newUser, 0, 0, 0);
    Room mockRoom = Room.builder().id(2L).user(newUser).theme(RoomTheme.BASIC).build();
    List<Furniture> mockFurnitures = new ArrayList<>();

    when(pointRepository.saveAndFlush(any(Point.class))).thenReturn(mockPoint);
    when(roomRepository.saveAndFlush(any(Room.class))).thenReturn(mockRoom);
    when(furnitureRepository.saveAllAndFlush(anyList())).thenReturn(mockFurnitures);
    when(userActivityService.recordUserActivity(anyLong(), any(ActivityType.class),
        any())).thenReturn(true);

    try (MockedStatic<OAuth2Factory> mockedStatic = mockStatic(OAuth2Factory.class)) {
      // OAuth2Factory 모킹
      OAuth2Response oAuth2Response = mock(OAuth2Response.class);
      lenient().when(oAuth2Response.getProviderId()).thenReturn(googleProviderId);
      lenient().when(oAuth2Response.getEmail()).thenReturn(TEST_EMAIL);
      lenient().when(oAuth2Response.getName()).thenReturn(TEST_NAME);
      lenient().when(oAuth2Response.getProfileImageUrl()).thenReturn(TEST_PROFILE_IMAGE);
      lenient().when(oAuth2Response.getProvider()).thenReturn(OAuth2Provider.GOOGLE);

      mockedStatic.when(() -> OAuth2Factory.createResponse(any(), any()))
          .thenReturn(oAuth2Response);

      // 기존 KAKAO 사용자가 있지만 GOOGLE 제공자로는 사용자 없음
      when(userRepository.findByProviderAndProviderId(Provider.GOOGLE, googleProviderId))
          .thenReturn(Optional.empty());
      when(userRepository.saveAndFlush(any(User.class))).thenReturn(newUser);
      when(userRepository.findById(anyLong())).thenReturn(Optional.of(newUser));

      // when
      OAuth2User result = customOAuth2UserService.processOAuth2User(userRequest, oAuth2User);

      // then
      assertThat(result).isInstanceOf(OAuth2UserPrincipal.class);
      OAuth2UserPrincipal principal = (OAuth2UserPrincipal) result;

      assertThat(principal.getUser()).satisfies(user -> {
        assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(user.getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(user.getProviderId()).isEqualTo(googleProviderId);
        assertThat(user.getId()).isEqualTo(2L); // 새 사용자 ID
      });

      verify(userRepository).findByProviderAndProviderId(Provider.GOOGLE, googleProviderId);
      verify(userRepository, atLeastOnce()).saveAndFlush(any(User.class));
      verify(pointRepository).saveAndFlush(any(Point.class));
      verify(roomRepository).saveAndFlush(any(Room.class));
      verify(furnitureRepository).saveAllAndFlush(anyList());
      verify(userActivityService).recordUserActivity(anyLong(), eq(ActivityType.ATTENDANCE),
          isNull());
    }
  }
}