package com.roome.domain.auth.controller;

import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.dto.response.MessageResponse;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.user.service.UserService;
import com.roome.global.exception.BusinessException;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import com.roome.global.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

  private final JwtTokenProvider jwtTokenProvider;
  private final TokenService tokenService;
  private final UserService userService;
  private final UserRepository userRepository;
  private final RedisService redisService;
  private final RoomService roomService;

  @Operation(
      summary = "사용자 정보 조회",
      description = "Access Token으로 사용자 정보를 조회합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패 또는 유효하지 않은 토큰")
  })
  @GetMapping("/user")
  public ResponseEntity<LoginResponse> getUserInfo(
      @RequestHeader("Authorization") String authHeader
  ) {
    try {
      String accessToken = authHeader.substring(7);
      Long userId = tokenService.getUserIdFromToken(accessToken);
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
      RoomResponseDto roomInfo = roomService.getOrCreateRoomByUserId(userId);

      String refreshToken = redisService.getRefreshToken(userId.toString());

      LoginResponse loginResponse = LoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .expiresIn(jwtTokenProvider.getAccessTokenExpirationTime() / 1000) // 초 단위로 변환
          .user(LoginResponse.UserInfo.builder()
              .userId(user.getId())
              .nickname(user.getNickname())
              .email(user.getEmail())
              .roomId(roomInfo.getRoomId())
              .profileImage(user.getProfileImage())
              .build())
          .build();

      return ResponseEntity.ok(loginResponse);
    } catch (Exception e) {
      log.error("사용자 정보 조회 중 오류: ", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(null);
    }
  }

  @Operation(
      summary = "로그아웃",
      description = "사용자를 로그아웃 처리하고 토큰을 무효화합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @Transactional
  @PostMapping("/logout")
  public ResponseEntity<?> logout(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    try {
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String accessToken = authHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        // 남은 유효 시간 계산
        long expiration = jwtTokenProvider.getTokenTimeToLive(accessToken);

        // Refresh Token 삭제
        redisService.deleteRefreshToken(userId);

        // Access Token 블랙리스트 추가 (남은 유효 시간만큼 유지)
        if (expiration > 0) {
          redisService.addToBlacklist(accessToken, expiration);
        }
      }

      return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    } catch (Exception e) {
      log.error("로그아웃 중 오류 발생: ", e);
      return ResponseEntity.internalServerError()
          .body(Map.of("message", "로그아웃 처리 중 오류가 발생했습니다."));
    }
  }

  @Operation(
      summary = "회원 탈퇴",
      description = "현재 로그인된 사용자 계정을 삭제합니다.",
      security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패 또는 유효하지 않은 토큰"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/withdraw")
  public ResponseEntity<MessageResponse> withdraw(
      @RequestHeader("Authorization") String authHeader) {
    try {
      String accessToken = authHeader.substring(7);

      if (accessToken.isBlank() || !jwtTokenProvider.validateAccessToken(accessToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new MessageResponse("유효하지 않은 액세스 토큰입니다."));
      }

      // 유저 ID 추출 및 회원 탈퇴 처리
      Long userId = tokenService.getUserIdFromToken(accessToken);
      userService.deleteUser(userId);

      // Refresh Token 삭제
      redisService.deleteRefreshToken(userId.toString());

      // Access Token 블랙리스트 추가
      redisService.addToBlacklist(accessToken, jwtTokenProvider.getAccessTokenExpirationTime());

      return ResponseEntity.ok(new MessageResponse("회원 탈퇴가 완료되었습니다."));

    } catch (InvalidRefreshTokenException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new MessageResponse(e.getMessage()));
    } catch (BusinessException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new MessageResponse(e.getMessage()));
    } catch (Exception e) {
      log.error("회원 탈퇴 중 오류 발생: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new MessageResponse("회원 탈퇴 처리 중 오류가 발생했습니다."));
    }
  }
}
