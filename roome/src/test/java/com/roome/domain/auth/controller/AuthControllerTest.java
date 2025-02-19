package com.roome.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.request.LoginRequest;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.exception.OAuth2AuthenticationProcessingException;
import com.roome.domain.auth.service.OAuth2LoginService;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import jakarta.servlet.http.HttpServletResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private TokenResponseHelper tokenResponseHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("카카오 로그인 요청 시 Access Token과 200을 반환한다.")
    void testLogin_Kakao_Success() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test-code");
        LoginResponse mockResponse = LoginResponse.builder()
                .accessToken("mockAccessToken")
                .refreshToken("mockRefreshToken")
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

        // When & Then
        mockMvc.perform(post("/api/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mockAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"))
                .andExpect(jsonPath("$.user.nickname").value("testUser"));
    }

    @Test
    @DisplayName("구글 로그인 요청 시 Access Token과 200 반환한다.")
    void testLogin_Google_Success() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("google-test-code");
        LoginResponse mockResponse = LoginResponse.builder()
                .accessToken("googleAccessToken")
                .refreshToken("googleRefreshToken")
                .expiresIn(3600L)
                .user(LoginResponse.UserInfo.builder()
                        .userId(2L)
                        .nickname("googleUser")
                        .email("google@example.com")
                        .roomId(2L)
                        .profileImage("google-profile.jpg")
                        .build())
                .build();

        when(oAuth2LoginService.login(OAuth2Provider.GOOGLE, "google-test-code")).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("googleAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("googleRefreshToken"))
                .andExpect(jsonPath("$.user.nickname").value("googleUser"));
    }

    @Test
    @DisplayName("존재하지 않는 OAuth Provider로 로그인 시 400을 응답한다.")
    void testLogin_InvalidProvider() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("invalid-code");

        when(oAuth2LoginService.login(eq(OAuth2Provider.KAKAO), any()))
                .thenThrow(new IllegalArgumentException("Invalid OAuth Provider"));

        // When & Then
        mockMvc.perform(post("/api/auth/login/invalidProvider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 인증 코드로 로그인 시 401을 응답한다.")
    void testLogin_InvalidCode() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("wrong-code");

        when(oAuth2LoginService.login(OAuth2Provider.KAKAO, "wrong-code"))
                .thenThrow(new OAuth2AuthenticationProcessingException());

        // When & Then
        mockMvc.perform(post("/api/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 성공 시 Access Token은 헤더에, Refresh Token은 쿠키에 존재한다.")
    void testLogin_TokenResponseHeaders() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test-code");
        HttpServletResponse response = mock(HttpServletResponse.class);
        JwtToken mockJwtToken = new JwtToken("mockAccessToken", "mockRefreshToken", "Bearer");

        doNothing().when(tokenResponseHelper).setTokenResponse(any(HttpServletResponse.class), eq(mockJwtToken));

        LoginResponse mockResponse = LoginResponse.builder()
                .accessToken("mockAccessToken")
                .refreshToken("mockRefreshToken")
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

        // When & Then
        mockMvc.perform(post("/api/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mockAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"));

        // Verify JWT Token 설정이 정상적으로 호출되었는지 확인
        verify(tokenResponseHelper, times(1)).setTokenResponse(any(HttpServletResponse.class), eq(mockJwtToken));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() throws Exception {
        // when
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());

        // then
        verify(tokenResponseHelper).removeTokenResponse(any(HttpServletResponse.class));
    }
}
