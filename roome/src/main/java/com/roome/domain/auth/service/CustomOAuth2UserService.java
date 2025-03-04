package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.point.entity.Point;
import com.roome.domain.point.entity.PointHistory;
import com.roome.domain.point.entity.PointReason;
import com.roome.domain.point.repository.PointHistoryRepository;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.point.repository.PointRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
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
  private final RoomRepository roomRepository;
  private final FurnitureRepository furnitureRepository;
  private final PointRepository pointRepository;
  private final PointHistoryRepository pointHistoryRepository;
  private final RoomService roomService;
  private final UserActivityService userActivityService;

  @Transactional
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
      LocalDateTime now = LocalDateTime.now();
      User user = updateOrCreateUser(oAuth2Response, now);

      // 하루 한 번 로그인 시 포인트 획득
      accumulateAttendancePoints(user, now);

      // 마지막 로그인 시간 갱신
      user.updateLastLogin(now);

      return new OAuth2UserPrincipal(user, oAuth2Response);
    } catch (IllegalArgumentException e) {
      String errorMsg =
          "지원하지 않는 OAuth2 제공자: " + userRequest.getClientRegistration().getRegistrationId();
      log.error(errorMsg, e);
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_provider"), errorMsg, e);

    } catch (NullPointerException e) {
      String errorMsg = "OAuth2 인증 데이터 누락: " + e.getMessage();
      log.error(errorMsg, e);
      log.error("OAuth2 요청 데이터: {}", userRequest);
      log.error("OAuth2 응답 데이터: {}", oAuth2User.getAttributes());
      throw new OAuth2AuthenticationException(new OAuth2Error("missing_data"), errorMsg, e);

    } catch (ClassCastException e) {
      String errorMsg = "OAuth2 데이터 형식 오류: " + e.getMessage();
      log.error(errorMsg, e);
      log.error("OAuth2 속성: {}", oAuth2User.getAttributes());
      throw new OAuth2AuthenticationException(new OAuth2Error("data_format_error"), errorMsg, e);

    } catch (OAuth2AuthenticationException e) {
      log.error("OAuth2 인증 실패: {}", e.getMessage(), e);
      throw e;

    } catch (Exception e) {
      StringBuilder errorDetail = new StringBuilder();
      errorDetail.append("예외 타입: ").append(e.getClass().getName()).append("\n");
      errorDetail.append("예외 메시지: ").append(e.getMessage()).append("\n");
      errorDetail.append("스택 트레이스:\n");

      for (StackTraceElement element : e.getStackTrace()) {
        errorDetail.append("   at ").append(element.toString()).append("\n");
      }

      if (e.getCause() != null) {
        errorDetail.append("원인 예외: ").append(e.getCause().getClass().getName()).append("\n");
        errorDetail.append("원인 메시지: ").append(e.getCause().getMessage()).append("\n");
      }

      log.error("OAuth2 처리 중 상세 오류 정보:\n{}", errorDetail.toString());

      String errorMsg = String.format("인증 처리 오류 [%s]: %s", e.getClass().getSimpleName(),
          e.getMessage());
      throw new OAuth2AuthenticationException(new OAuth2Error("auth_processing_error"), errorMsg,
          e);
    }
  }

  private void accumulateAttendancePoints(User user, LocalDateTime now) {
    // 포인트 지급 (하루에 한 번)
    if (!user.isAttendanceToday(now)) {
      // 오늘 처음 로그인한 경우에만 포인트 지급
      int point = new Random().nextInt(50) + 1;
      user.accumulatePoints(point);
      pointHistoryRepository.save(
          PointHistory.builder().user(user).amount(point).reason(PointReason.DAILY_ATTENDANCE)
              .build());
    }

    // 랭킹 출석 체크 (오전/오후)
    recordAttendanceForRanking(user.getId());
  }

  // 랭킹 시스템을 위한 출석 체크
  private void recordAttendanceForRanking(Long userId) {
    userActivityService.recordUserActivity(userId, ActivityType.ATTENDANCE, null);
  }

  private User updateOrCreateUser(OAuth2Response response, LocalDateTime now) {
    Provider provider = Provider.valueOf(response.getProvider().name());
    String providerId = response.getProviderId();

    // provider + providerId 로 기존 유저 확인
    return userRepository.findByProviderAndProviderId(provider, providerId).orElseGet(() -> {
      log.info("새 사용자 생성: 이메일={}, 제공자={}", response.getEmail(), provider);

      try {
        // 1. User 객체 생성
        User newUser = User.createWithoutRelations(
            response.getName(),
            response.getName(),
            response.getEmail(),
            response.getProfileImageUrl(),
            provider,
            providerId,
            Status.ONLINE
        );
        newUser.setLastLogin(now);

        // 2. User 저장
        User savedUser = userRepository.saveAndFlush(newUser);

        // 3. Point 생성 및 저장
        Point point = Point.init(savedUser, now);
        point = pointRepository.saveAndFlush(point);

        // 4. Room 생성 및 저장
        Room room = Room.createRoom(savedUser, RoomTheme.BASIC);
        room.setCreatedAt(now);
        // id가 자동으로 설정되지 않는 경우 명시적으로 설정
        // room.setId(savedUser.getId());
        room = roomRepository.saveAndFlush(room);

        // 5. Furniture 생성 및 저장
        List<Furniture> furnitures = Furniture.createDefaultFurnitures(room, now);
        furnitureRepository.saveAllAndFlush(furnitures);

        // 6. User에 Room과 Point 설정
        savedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        savedUser.setRoom(room);
        savedUser.setPoint(point);

        // 7. User 저장
        return userRepository.saveAndFlush(savedUser);
      } catch (Exception e) {
        log.error("새 사용자 생성 중 오류 발생: {}", e.getMessage(), e);
        throw new RuntimeException("사용자 생성 실패: " + e.getMessage(), e);
      }
    });
  }
}