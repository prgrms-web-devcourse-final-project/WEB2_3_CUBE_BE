package com.roome.global.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.User;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final RoomService roomService;
    private final ObjectMapper objectMapper;
    private final TokenResponseHelper tokenResponseHelper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2UserPrincipal oAuth2UserPrincipal = (OAuth2UserPrincipal) authentication.getPrincipal();
        User user = oAuth2UserPrincipal.getUser();

        log.info("OAuth2 로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

        // JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.createToken(user.getId().toString());

        // Redis에 리프레시 토큰 저장
        redisService.saveRefreshToken(
                user.getId().toString(),
                jwtToken.getRefreshToken(),
                jwtTokenProvider.getRefreshTokenExpirationTime()
        );

        try {
            // 방 정보 조회
            RoomResponseDto roomInfo = roomService.getOrCreateRoomByUserId(user.getId());

            // 응답 객체 생성
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(jwtToken.getAccessToken())
                    .refreshToken(jwtToken.getRefreshToken())
                    .expiresIn(3600L)
                    .user(LoginResponse.UserInfo.builder()
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .email(user.getEmail())
                            .roomId(roomInfo.getRoomId())
                            .profileImage(user.getProfileImage())
                            .build())
                    .build();

            // 토큰을 쿠키/헤더에 설정
            tokenResponseHelper.setTokenResponse(response, jwtToken);

            // 응답 설정
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(loginResponse));

        } catch (Exception e) {
            log.error("OAuth2 성공 처리 중 오류: {}", e.getMessage(), e);

            // 오류 발생해도 최소한의 로그인 응답은 전송
            Map<String, Object> errorResponse = Map.of(
                    "accessToken", jwtToken.getAccessToken(),
                    "refreshToken", jwtToken.getRefreshToken(),
                    "expiresIn", 3600L,
                    "error", "방 정보를 불러오는 중 오류가 발생했습니다."
            );

            // 토큰을 쿠키/헤더에 설정
            tokenResponseHelper.setTokenResponse(response, jwtToken);

            // 응답 설정
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}