package com.roome.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.roome.domain.user.service.UserService;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import com.roome.global.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WithdrawControllerTest {

  @InjectMocks
  private AuthController authController;

  @Mock
  private UserService userService;

  @Mock
  private TokenService tokenService;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private RedisService redisService;

  private MockMvc mockMvc;

  private static final String VALID_TOKEN = "valid.access.token";
  private static final Long USER_ID = 1L;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    lenient().when(jwtTokenProvider.validateAccessToken(VALID_TOKEN)).thenReturn(true);
    lenient().when(tokenService.getUserIdFromToken(VALID_TOKEN)).thenReturn(USER_ID);
  }

  @Test
  @DisplayName("회원 탈퇴 성공 테스트")
  void withdrawSuccess() throws Exception {
    mockMvc.perform(delete("/auth/withdraw").header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));

    verify(userService, times(1)).deleteUser(USER_ID);
  }

  @Test
  @DisplayName("유효하지 않은 토큰으로 회원 탈퇴 시도")
  void withdrawWithInvalidToken() throws Exception {
    String invalidToken = "invalid.token";
    lenient().when(jwtTokenProvider.validateAccessToken(invalidToken)).thenReturn(false);

    mockMvc.perform(delete("/auth/withdraw").header(AUTHORIZATION, "Bearer " + invalidToken)
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

    verify(userService, times(0)).deleteUser(any());
  }

  @Test
  @DisplayName("사용자 서비스에서 비즈니스 예외 발생 시 회원 탈퇴 실패")
  void withdrawFailsWhenUserServiceThrowsBusinessException() throws Exception {
    doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND)).when(userService).deleteUser(USER_ID);

    mockMvc.perform(delete("/auth/withdraw").header(AUTHORIZATION, "Bearer " + VALID_TOKEN)
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
  }
}