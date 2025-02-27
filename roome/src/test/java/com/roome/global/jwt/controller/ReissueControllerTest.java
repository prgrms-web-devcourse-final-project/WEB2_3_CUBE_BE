package com.roome.global.jwt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReissueController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class ReissueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰 재발급 성공")
    void reissueToken_Success() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";
        JwtToken newToken = new JwtToken(
                "Bearer",
                "new-access-token",
                "new-refresh-token"
        );

        Map<String, String> requestBody = Map.of("refreshToken", refreshToken);

        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(tokenService.reissueToken(refreshToken)).thenReturn(newToken);
        when(jwtTokenProvider.getAccessTokenExpirationTime()).thenReturn(3600000L);

        // When & Then
        mockMvc.perform(post("/api/auth/reissue-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("리프레시 토큰이 없는 경우 재발급 실패")
    void reissueToken_NoRefreshToken_Failure() throws Exception {
        // Given
        Map<String, String> requestBody = Map.of();

        // When & Then
        mockMvc.perform(post("/api/auth/reissue-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 필요합니다."));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 실패")
    void reissueToken_InvalidRefreshToken_Failure() throws Exception {
        // Given
        String refreshToken = "invalid-refresh-token";
        Map<String, String> requestBody = Map.of("refreshToken", refreshToken);

        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/reissue-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
    }
}