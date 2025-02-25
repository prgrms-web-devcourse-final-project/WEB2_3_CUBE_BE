package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.exception.OAuth2AuthenticationProcessingException;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2LoginService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoomService roomService;
    private final RestTemplate restTemplate;
    private final OAuth2ClientProperties oauth2ClientProperties;

    public LoginResponse login(OAuth2Provider provider, String authorizationCode) {
        try {
            // OAuth2 Access Token 요청
            String accessToken = fetchAccessToken(provider, authorizationCode);

            // Access Token을 이용하여 사용자 정보 요청
            OAuth2Response oAuth2Response = fetchUserProfile(provider, accessToken);

            User user = updateOrCreateUser(oAuth2Response);

            // 첫 로그인 시 방 자동 생성
            if (user.getLastLogin() == null) {
                roomService.createRoom(user.getId());
            }

            // 하루 한 번 로그인시 포인트 획득
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
            if (!user.isAttendanceToday(now)) {
                // todo 포인트 획득
            }
            userRepository.updateLastLogin(user.getId(), now);

            // JWT 토큰 발급
            JwtToken jwtToken = generateJwtToken(user);

            return buildLoginResponse(user, jwtToken);
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생: ", e);
            throw new OAuth2AuthenticationProcessingException();
        }
    }

    private String fetchAccessToken(OAuth2Provider provider, String authorizationCode) {
        try {

            // Provider별 코드 디코딩 처리
            String code = provider.requiresDecoding()
                    ? URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8)
                    : authorizationCode;

            var registration = oauth2ClientProperties.getRegistration()
                    .get(provider.getRegistrationId());

            // 토큰 요청 파라미터 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", registration.getClientId());
            params.add("client_secret", registration.getClientSecret());
            params.add("code", code);
            params.add("redirect_uri", registration.getRedirectUri());

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    provider.getTokenUri(),
                    new HttpEntity<>(params, headers),
                    Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || !response.getBody().containsKey("access_token")) {
                log.error("액세스 토큰을 가져오지 못했습니다: {}", response.getBody());
                throw new OAuth2AuthenticationProcessingException();
            }

            return (String) response.getBody().get("access_token");
        } catch (RestClientException e) {
            log.error("액세스 토큰을 가져오지 못했습니다.: {}", e.getMessage());
            throw new OAuth2AuthenticationProcessingException();
        }
    }

    private OAuth2Response fetchUserProfile(OAuth2Provider provider, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                provider.getUserInfoUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        return provider.createOAuth2Response(response.getBody());
    }

    private User updateOrCreateUser(OAuth2Response response) {
        return userRepository.findByEmail(response.getEmail())
                .map(user -> {
                    // 기존 유저 정보 업데이트
                    user.updateProfile(response.getName(), response.getProfileImageUrl(), user.getBio());
                    user.updateProvider(Provider.valueOf(response.getProvider().name()));
                    user.updateProviderId(response.getProviderId());
                    user.updateLastLogin();
                    return user;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(response.getName())
                                .nickname(response.getName())
                                .email(response.getEmail())
                                .profileImage(response.getProfileImageUrl())
                                .provider(Provider.valueOf(response.getProvider().name()))
                                .providerId(response.getProviderId())
                                .status(Status.OFFLINE)
                                .build()
                ));
    }

    private JwtToken generateJwtToken(User user) {
        return jwtTokenProvider.createToken(user.getId().toString());
    }

    private LoginResponse buildLoginResponse(User user, JwtToken jwtToken) {
        return LoginResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .roomId(roomService.getRoomByUserId(user.getId()).getRoomId())
                        .profileImage(user.getProfileImage())
                        .build())
                .build();
    }
}