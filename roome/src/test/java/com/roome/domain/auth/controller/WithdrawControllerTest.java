package com.roome.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.user.service.UserService;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import com.roome.global.service.RedisService;
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

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class WithdrawControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private RedisService redisService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoomService roomService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("회원 탈퇴 성공 시 Access Token 블랙리스트 추가 및 Refresh Token 삭제")
    void withdrawSuccess_BlacklistAndDeleteToken() throws Exception {
        // Given
        String accessToken = "mockAccessToken";
        Long userId = 1L;

        when(jwtTokenProvider.validateAccessToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.getAccessTokenExpirationTime()).thenReturn(3600000L); // 1시간
        when(tokenService.getUserIdFromToken(accessToken)).thenReturn(userId);
        doNothing().when(redisService).deleteRefreshToken(userId.toString());
        doNothing().when(redisService).addToBlacklist(eq(accessToken), anyLong());
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));

        // Verify: 블랙리스트 추가 & Refresh Token 삭제가 호출되었는지 확인
        verify(redisService, times(1)).deleteRefreshToken(userId.toString());
        verify(redisService, times(1)).addToBlacklist(eq(accessToken), anyLong());
    }

    @Test
    @DisplayName("유효하지 않은 Access Token으로 회원 탈퇴 시 401 반환")
    void withdrawWithInvalidAccessToken_Returns401() throws Exception {
        // Given
        String accessToken = "invalidAccessToken";

        when(jwtTokenProvider.validateAccessToken(accessToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 액세스 토큰입니다."));

        // Verify: 토큰이 유효하지 않아 호출되지 않음
        verify(redisService, times(0)).deleteRefreshToken(anyString());
        verify(userService, times(0)).deleteUser(anyLong());
    }

    @Test
    @DisplayName("Authorization 헤더 없이 회원 탈퇴 시 401 반환")
    void withdrawFail_NoAuthorizationToken() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/auth/withdraw")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}