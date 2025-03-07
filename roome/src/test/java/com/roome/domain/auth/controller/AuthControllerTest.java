package com.roome.domain.auth.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.user.service.UserService;
import com.roome.domain.user.service.UserStatusService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

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

  @MockBean
  private FurnitureRepository furnitureRepository;

  @MockBean
  private UserStatusService userStatusService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("로그아웃 시 Access Token이 Redis 블랙리스트에 추가된다.")
  @WithMockUser(username = "1")
    // Spring Security에서 제공하는 어노테이션 사용
  void logoutSuccess_RedisBlacklist() throws Exception {
    // Given
    String accessToken = "mockAccessToken";
    String userId = "1";
    long expiration = 3600000L; // 1시간

    when(jwtTokenProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
    when(jwtTokenProvider.getTokenTimeToLive(accessToken)).thenReturn(expiration);
    when(redisService.deleteRefreshToken(anyString())).thenReturn(true);
    when(redisService.addToBlacklist(anyString(), anyLong())).thenReturn(true);

    // When & Then
    mockMvc.perform(post("/auth/logout")
            .header("Authorization", "Bearer " + accessToken)
            .with(csrf()))
        .andExpect(status().isOk());

    // Verify 블랙리스트 추가 확인
    verify(redisService, times(1)).addToBlacklist(eq(accessToken), eq(expiration));
    verify(redisService, times(1)).deleteRefreshToken(eq(userId));
  }

  @Test
  @DisplayName("로그아웃 시 Redis에서 Refresh Token이 삭제된다.")
  @WithMockUser(username = "1")
    // Spring Security에서 제공하는 어노테이션 사용
  void logoutSuccess_RedisTokenDeleted() throws Exception {
    // Given
    String accessToken = "mockAccessToken";
    String userId = "1";
    long expiration = 3600000L; // 1시간

    when(jwtTokenProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
    when(jwtTokenProvider.getTokenTimeToLive(accessToken)).thenReturn(expiration);
    when(redisService.deleteRefreshToken(anyString())).thenReturn(true);
    when(redisService.addToBlacklist(anyString(), anyLong())).thenReturn(true);

    // When & Then
    mockMvc.perform(post("/auth/logout")
            .header("Authorization", "Bearer " + accessToken)
            .with(csrf()))
        .andExpect(status().isOk());

    // Verify Redis에서 Refresh Token 삭제 확인
    verify(redisService, times(1)).deleteRefreshToken(eq(userId));
    verify(redisService, times(1)).addToBlacklist(eq(accessToken), eq(expiration));
  }

  @Test
  @DisplayName("잘못된 Access Token으로 로그아웃 시 Redis 블랙리스트에 등록되지 않는다.")
  @WithMockUser(username = "1")
    // Spring Security에서 제공하는 어노테이션 사용
  void logoutFail_InvalidAccessToken() throws Exception {
    // Given
    String invalidToken = "invalidAccessToken";

    when(jwtTokenProvider.getUserIdFromToken(invalidToken)).thenThrow(
        new IllegalArgumentException("Invalid Token"));

    // When & Then
    mockMvc.perform(post("/auth/logout")
            .header("Authorization", "Bearer " + invalidToken)
            .with(csrf()))
        .andExpect(status().isInternalServerError());

    // Verify Redis 블랙리스트 등록이 호출되지 않음
    verify(redisService, times(0)).addToBlacklist(anyString(), anyLong());
  }
}