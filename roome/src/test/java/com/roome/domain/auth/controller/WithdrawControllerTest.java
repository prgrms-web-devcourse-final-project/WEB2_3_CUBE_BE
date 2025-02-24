package com.roome.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.auth.service.OAuth2LoginService;
import com.roome.domain.user.service.UserService;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import com.roome.global.service.RedisService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class WithdrawControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2LoginService oAuth2LoginService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private RedisService redisService;

    @MockBean
    private TokenResponseHelper tokenResponseHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("회원 탈퇴 성공 시 Access Token 블랙리스트 추가 및 Refresh Token 삭제")
    void withdrawSuccess_BlacklistAndDeleteToken() throws Exception {
        // Given
        String accessToken = "mockAccessToken";
        String refreshToken = "mockRefreshToken";
        Long userId = 1L;

        when(jwtTokenProvider.validateAccessToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getAccessTokenExpirationTime()).thenReturn(3600000L); // 1시간 설정
        when(tokenService.getUserIdFromToken(accessToken)).thenReturn(userId);
        doNothing().when(redisService).deleteRefreshToken(userId.toString());
        doNothing().when(redisService).addToBlacklist(eq(accessToken), anyLong());
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));

        // Verify: 블랙리스트 추가 & Refresh Token 삭제가 호출되었는지 확인
        verify(redisService, times(1)).deleteRefreshToken(userId.toString());
        verify(redisService, times(1)).addToBlacklist(eq(accessToken), anyLong());
    }

    @Test
    @DisplayName("Access Token이 만료되었을 때 Refresh Token 검증 후 재발급")
    void withdrawWithExpiredAccessToken_ReissueToken() throws Exception {
        // Given
        String accessToken = "expiredAccessToken";
        String refreshToken = "validRefreshToken";
        Long userId = 1L;
        JwtToken newToken = new JwtToken("newAccessToken", "newRefreshToken", "Bearer");

        when(jwtTokenProvider.validateAccessToken(accessToken)).thenReturn(false); // 액세스 토큰 만료
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(true); // 리프레시 토큰 유효
        when(jwtTokenProvider.getAccessTokenExpirationTime()).thenReturn(3600000L);
        when(tokenService.reissueToken(refreshToken)).thenReturn(newToken);
        when(tokenService.getUserIdFromToken(newToken.getAccessToken())).thenReturn(userId);
        doNothing().when(redisService).deleteRefreshToken(userId.toString());
        doNothing().when(redisService).addToBlacklist(eq(newToken.getAccessToken()), anyLong());
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));

        // Verify: 새 Access Token을 사용하여 회원 탈퇴 처리
        verify(redisService, times(1)).deleteRefreshToken(userId.toString());
        verify(redisService, times(1)).addToBlacklist(eq(newToken.getAccessToken()), anyLong());
    }

    @Test
    @DisplayName("Refresh Token이 없거나 유효하지 않을 때 탈퇴 실패 (401)")
    void withdrawFail_InvalidRefreshToken() throws Exception {
        // Given
        String accessToken = "expiredAccessToken";
        String refreshToken = "invalidRefreshToken";

        when(jwtTokenProvider.validateAccessToken(accessToken)).thenReturn(false);
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(false); // 리프레시 토큰도 유효하지 않음

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(new Cookie("refresh_token", refreshToken))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 없거나 유효하지 않습니다."));

        // Verify: Refresh Token이 유효하지 않아 삭제되지 않음
        verify(redisService, times(0)).deleteRefreshToken(anyString());
    }

    @Test
    @DisplayName("Authorization 헤더 없이 회원 탈퇴 시 401 반환")
    void withdrawFail_NoAuthorizationToken() throws Exception {
        // Given
        String refreshToken = "validRefreshToken";

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .cookie(new Cookie("refresh_token", refreshToken)) // ✅ 쿠키는 있지만 Authorization 없음
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
