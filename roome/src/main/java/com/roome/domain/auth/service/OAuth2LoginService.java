package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.oauth2.OAuth2Response;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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

    public LoginResponse login(OAuth2Provider provider, String authorizationCode) {

        // OAuth2 Access Token 요청
        String accessToken = fetchAccessToken(provider, authorizationCode);

        // Access Token을 이용하여 사용자 정보 요청
        OAuth2Response oAuth2Response = fetchUserProfile(provider, accessToken);

        User user = updateOrCreateUser(oAuth2Response);

/*

        // 첫 로그인 시 방 자동 생성
        if (user.getLastLogin() == null) {
            roomService.createRoom(user);
        }
*/

        // Authentication 객체 생성 후 JWT 발급
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getProviderId(), null);
        JwtToken jwtToken = jwtTokenProvider.createToken(authentication);

        return LoginResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
//                        .roomId(roomService.getRoomIdByUserId(user.getId()))
                        .profileImage(user.getProfileImage())
                        .build())
                .build();
    }

    private String fetchAccessToken(OAuth2Provider provider, String authorizationCode) {
        Map<String, Object> response = restTemplate.postForObject(
                provider.getTokenUri(),
                provider.getTokenRequest(authorizationCode),
                Map.class
        );
        return (String) response.get("access_token");
    }

    private OAuth2Response fetchUserProfile(OAuth2Provider provider, String accessToken) {
        Map<String, Object> attributes = restTemplate.getForObject(
                provider.getUserInfoUri(accessToken),
                Map.class
        );
        return provider.createOAuth2Response(attributes);
    }

    private User updateOrCreateUser(OAuth2Response response) {
        return userRepository.findByProviderId(response.getProviderId())
                .map(user -> {
                    user.updateProfile(response.getName(), response.getProfileImageUrl(), user.getBio());
                    return user;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(response.getName())
                                .nickname(response.getName())
                                .profileImage(response.getProfileImageUrl())
                                .provider(Provider.valueOf(response.getProvider().name()))
                                .providerId(response.getProviderId())
                                .status(Status.OFFLINE)
                                .build()
                ));
    }
}
