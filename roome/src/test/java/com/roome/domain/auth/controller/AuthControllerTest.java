package com.roome.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.request.LoginRequest;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.service.OAuth2LoginService;
import com.roome.domain.user.service.UserService;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import com.roome.global.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2LoginService oAuth2LoginService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenResponseHelper tokenResponseHelper;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private RedisService redisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("로그인 성공 시 Redis에 Refresh Token이 저장된다.")
    void loginSuccess_RedisTokenStored() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test-code");
        JwtToken jwtToken = new JwtToken("mockAccessToken", "mockRefreshToken", "Bearer");
        LoginResponse mockResponse = LoginResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .userId(1L)
                        .nickname("testUser")
                        .email("test@example.com")
                        .roomId(1L)
                        .profileImage("profile.jpg")
                        .build())
                .build();

        when(oAuth2LoginService.login(OAuth2Provider.KAKAO, "test-code")).thenReturn(mockResponse);
        doNothing().when(redisService).saveRefreshToken(anyString(), anyString(), anyLong());

        // When & Then
        mockMvc.perform(post("/api/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(jwtToken.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(jwtToken.getRefreshToken()));

        // Verify Redis 저장 호출 확인
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> refreshTokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> expirationCaptor = ArgumentCaptor.forClass(Long.class);

        verify(redisService, times(1))
                .saveRefreshToken(userIdCaptor.capture(), refreshTokenCaptor.capture(), expirationCaptor.capture());

        // Verify 올바른 User ID와 Refresh Token이 저장되었는지 확인
        assert userIdCaptor.getValue().equals("1");
        assert refreshTokenCaptor.getValue().equals(jwtToken.getRefreshToken());
    }

    @Test
    @DisplayName("로그아웃 시 Access Token이 Redis 블랙리스트에 추가된다.")
    void logoutSuccess_RedisBlacklist() throws Exception {
        // Given
        String accessToken = "mockAccessToken";
        String userId = "1";

        when(jwtTokenProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
        doNothing().when(redisService).addToBlacklist(anyString(), anyLong());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verify 블랙리스트 추가 확인
        verify(redisService, times(1)).addToBlacklist(eq(accessToken), anyLong());
    }

    @Test
    @DisplayName("로그아웃 시 Redis에서 Refresh Token이 삭제된다.")
    void logoutSuccess_RedisTokenDeleted() throws Exception {
        // Given
        String accessToken = "mockAccessToken";
        String userId = "1";

        when(jwtTokenProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
        doNothing().when(redisService).deleteRefreshToken(userId);
        doNothing().when(redisService).addToBlacklist(anyString(), anyLong());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verify Redis에서 Refresh Token 삭제 확인
        verify(redisService, times(1)).deleteRefreshToken(eq(userId));

        // Verify 블랙리스트 추가 확인
        verify(redisService, times(1)).addToBlacklist(eq(accessToken), anyLong());
    }

    @Test
    @DisplayName("잘못된 Access Token으로 로그아웃 시 Redis 블랙리스트에 등록되지 않는다.")
    void logoutFail_InvalidAccessToken() throws Exception {
        // Given
        String invalidToken = "invalidAccessToken";

        when(jwtTokenProvider.getUserIdFromToken(invalidToken)).thenThrow(new IllegalArgumentException("Invalid Token"));

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + invalidToken)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        // Verify Redis 블랙리스트 등록이 호출되지 않음
        verify(redisService, times(0)).addToBlacklist(anyString(), anyLong());
    }
}
