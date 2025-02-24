package com.roome.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.user.service.UserService;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WithdrawControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenResponseHelper tokenResponseHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String validAccessToken = "validAccessToken";
    private final String expiredAccessToken = "expiredAccessToken";
    private final String validRefreshToken = "validRefreshToken";
    private final String invalidRefreshToken = "invalidRefreshToken";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        lenient().when(jwtTokenProvider.validateAccessToken(anyString())).thenReturn(true);
        lenient().when(tokenService.getUserIdFromToken(anyString())).thenReturn(1L);
    }

    @Test
    @DisplayName("회원 탈퇴 - 정상 요청")
    void withdraw_Success() throws Exception {
        // Given
        when(jwtTokenProvider.validateAccessToken(validAccessToken)).thenReturn(true);
        when(tokenService.getUserIdFromToken(validAccessToken)).thenReturn(1L);
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 - 유효하지 않은 액세스 토큰 요청")
    void withdraw_InvalidAccessToken() throws Exception {
        // Given
        when(jwtTokenProvider.validateAccessToken(validAccessToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 없거나 유효하지 않습니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 - 만료된 액세스 토큰 + 유효한 리프레시 토큰 사용")
    void withdraw_ExpiredAccessToken_WithValidRefreshToken() throws Exception {
        // Given
        JwtToken newToken = new JwtToken("newAccessToken", validRefreshToken, "Bearer");

        when(jwtTokenProvider.validateAccessToken(expiredAccessToken)).thenReturn(false);
        when(jwtTokenProvider.validateRefreshToken(validRefreshToken)).thenReturn(true);
        when(tokenService.reissueToken(validRefreshToken)).thenReturn(newToken);
        doReturn(1L).when(tokenService).getUserIdFromToken(anyString());

        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + expiredAccessToken)
                        .cookie(new Cookie("refresh_token", validRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenService, times(1)).getUserIdFromToken(tokenCaptor.capture());

        // 디버깅용
        System.out.println("Captured Token: " + tokenCaptor.getValue());
    }

    @Test
    @DisplayName("회원 탈퇴 - 유효하지 않은 리프레시 토큰 사용 시 실패")
    void withdraw_InvalidRefreshToken() throws Exception {
        // Given
        when(jwtTokenProvider.validateAccessToken(expiredAccessToken)).thenReturn(false);
        when(jwtTokenProvider.validateRefreshToken(invalidRefreshToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + expiredAccessToken)
                        .cookie(new Cookie("refresh_token", invalidRefreshToken)))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 없거나 유효하지 않습니다."));

    }

    @Test
    @DisplayName("회원 탈퇴 - 존재하지 않는 사용자 ID 요청 시 실패")
    void withdraw_UserNotFound() throws Exception {
        // Given
        when(jwtTokenProvider.validateAccessToken(validAccessToken)).thenReturn(true);
        when(tokenService.getUserIdFromToken(validAccessToken)).thenReturn(99L); // 존재하지 않는 ID
        doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND)).when(userService).deleteUser(99L);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴 - 탈퇴 시 토큰 삭제 확인")
    void withdraw_TokenRemoval() throws Exception {
        // Given
        when(jwtTokenProvider.validateAccessToken(validAccessToken)).thenReturn(true);
        when(tokenService.getUserIdFromToken(validAccessToken)).thenReturn(1L);
        doNothing().when(userService).deleteUser(1L);
        doNothing().when(tokenResponseHelper).removeTokenResponse(any(HttpServletResponse.class));

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));

        verify(tokenResponseHelper, times(1)).removeTokenResponse(any(HttpServletResponse.class));
    }
}
