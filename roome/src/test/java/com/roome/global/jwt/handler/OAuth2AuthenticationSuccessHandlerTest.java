package com.roome.global.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisService redisService;

    @Mock
    private RoomService roomService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TokenResponseHelper tokenResponseHelper;

    @Test
    @DisplayName("인증 성공 시 JWT 토큰이 발급되고 Redis에 저장된다")
    void onAuthenticationSuccess_GeneratesTokenAndSavesToRedis() throws IOException {
        // Given
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .nickname("testuser")
                .provider(Provider.KAKAO)
                .providerId("12345")
                .status(Status.OFFLINE)
                .lastLogin(LocalDateTime.now())
                .build();

        OAuth2UserPrincipal oAuth2UserPrincipal = mock(OAuth2UserPrincipal.class);
        when(oAuth2UserPrincipal.getUser()).thenReturn(user);

        Authentication authentication = new TestingAuthenticationToken(oAuth2UserPrincipal, null);

        JwtToken jwtToken = JwtToken.builder()
                .grantType("Bearer")
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .build();

        RoomResponseDto roomResponseDto = RoomResponseDto.builder()
                .roomId(1L)
                .userId(1L)
                .theme("BASIC")
                .furnitures(Collections.emptyList())
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .roomId(roomResponseDto.getRoomId())
                        .profileImage(user.getProfileImage())
                        .build())
                .build();

        String loginResponseJson = "{\"accessToken\":\"test-access-token\"}";

        when(jwtTokenProvider.createToken(anyString())).thenReturn(jwtToken);
        when(roomService.getOrCreateRoomByUserId(anyLong())).thenReturn(roomResponseDto);
        when(objectMapper.writeValueAsString(any(LoginResponse.class))).thenReturn(loginResponseJson);
        doNothing().when(tokenResponseHelper).setTokenResponse(any(), any());
        doNothing().when(redisService).saveRefreshToken(anyString(), anyString(), anyLong());

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(jwtTokenProvider).createToken(user.getId().toString());
        verify(redisService).saveRefreshToken(
                eq(user.getId().toString()),
                eq(jwtToken.getRefreshToken()),
                anyLong()
        );
        verify(tokenResponseHelper).setTokenResponse(response, jwtToken);
        verify(objectMapper).writeValueAsString(any(LoginResponse.class));
    }
}