package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final RoomService roomService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    return processOAuth2User(userRequest, oAuth2User);
  }

  @Transactional
  public OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
    try {
      String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
      OAuth2Provider provider = OAuth2Provider.valueOf(registrationId);

      OAuth2Response oAuth2Response = OAuth2Factory.createResponse(provider,
          oAuth2User.getAttributes());

      // 기존 사용자 확인 또는 생성
      User user = updateOrCreateUser(oAuth2Response);

      // 마지막 로그인 시간 갱신
      user.updateLastLogin();
      if (user.getId() != null) {
        userRepository.save(user);
      }

      // 하루 한 번 로그인 시 포인트 획득
//      accumulateAttendancePoints(user);

      return new OAuth2UserPrincipal(user, oAuth2Response);
    } catch (OAuth2AuthenticationException e) {
      log.error("OAuth2 인증 중 오류 발생: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("OAuth2 처리 중 내부 오류: {}", e.getMessage());
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "OAuth2 인증 실패", e);
    }
  }

  private void accumulateAttendancePoints(User user) {
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    if (!user.isAttendanceToday(now)) {
      int point = new Random().nextInt(50) + 1;
      user.accumulatePoints(point);

      pointHistoryRepository.save(
          PointHistory.builder().user(user).amount(point).reason(PointReason.DAILY_ATTENDANCE)
              .build());
    }
  }

  private User updateOrCreateUser(OAuth2Response response) {
    Provider provider = Provider.valueOf(response.getProvider().name());
    String providerId = response.getProviderId();

    // provider id로 사용자 찾기
    return userRepository.findByProviderAndProviderId(provider, providerId).map(existingUser -> {
      // 기존 사용자 정보 업데이트
      existingUser.updateProfile(response.getName(), response.getProfileImageUrl(),
          existingUser.getBio());
      return existingUser;
    }).orElseGet(() -> {
      // provider 다르면 이메일이 같아도 새 계정 생성
      log.info("새 사용자 생성: 이메일={}, 제공자={}", response.getEmail(), provider);
      return userRepository.save(
          User.builder().name(response.getName()).nickname(response.getName())
              .email(response.getEmail()).profileImage(response.getProfileImageUrl())
              .provider(provider).providerId(providerId).status(Status.OFFLINE)
              .lastLogin(LocalDateTime.now()).point(new Point(null, 0, 0, 0)).build());
    });
  }
}
