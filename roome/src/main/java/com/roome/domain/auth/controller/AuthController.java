package com.roome.domain.auth.controller;

import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.dto.response.MessageResponse;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.user.service.UserService;
import com.roome.domain.user.service.UserStatusService;
import com.roome.global.auth.AuthenticatedUser;
import com.roome.global.exception.BusinessException;
import com.roome.global.jwt.exception.InvalidJwtTokenException;
import com.roome.global.jwt.exception.InvalidUserIdFormatException;
import com.roome.global.jwt.exception.MissingUserIdFromTokenException;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import com.roome.global.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
  private final UserStatusService userStatusService;

  @Operation(summary = "사용자 정보 조회", description = "Access Token으로 사용자 정보를 조회합니다.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 실패 또는 유효하지 않은 토큰")})
  @GetMapping("/user")
  public ResponseEntity<LoginResponse> getUserInfo(
      @RequestHeader("Authorization") String authHeader) {
    try {
      String accessToken = authHeader.substring(7);
      Long userId = tokenService.getUserIdFromToken(accessToken);
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
      RoomResponseDto roomInfo = roomService.getOrCreateRoomByUserId(userId);

      String refreshToken = redisService.getRefreshToken(userId.toString());
      log.info("User ID: {}, Refresh Token: {}", userId, refreshToken);

      LoginResponse loginResponse = LoginResponse.builder().accessToken(accessToken)
          .refreshToken(refreshToken)
          .expiresIn(jwtTokenProvider.getAccessTokenExpirationTime() / 1000) // 초 단위로 변환
          .user(LoginResponse.UserInfo.builder().userId(user.getId()).nickname(user.getNickname())
              .email(user.getEmail()).roomId(roomInfo.getRoomId())
              .profileImage(user.getProfileImage()).build()).build();

      return ResponseEntity.ok(loginResponse);
    } catch (Exception e) {
      log.error("사용자 정보 조회 중 오류: ", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
  }

  @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리하고 토큰을 무효화합니다.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
  @Transactional
  @PostMapping("/logout")
  public ResponseEntity<?> logout(
      @RequestHeader(value = "Authorization", required = false) String authHeader,
      @AuthenticatedUser Long authenticatedUserId,
      HttpServletResponse response) {
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

        // 리프레시 토큰 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "").maxAge(0).path("/")
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
      }
      // 사용자 상태 변경
      userStatusService.updateUserStatus(authenticatedUserId, Status.OFFLINE);
      log.info("사용자 상태 변경: userId={}, status={}", authenticatedUserId, Status.OFFLINE);
      return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    } catch (Exception e) {
      log.error("로그아웃 중 오류 발생: ", e);
      return ResponseEntity.internalServerError().body(Map.of("message", "로그아웃 처리 중 오류가 발생했습니다."));
    }
  }

  @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자 계정을 삭제합니다.", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패 또는 유효하지 않은 토큰"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
  @DeleteMapping("/withdraw")
  @Transactional // 트랜잭션 경계 명확화
  public ResponseEntity<MessageResponse> withdraw(
      @RequestHeader("Authorization") String authHeader) {

    // 1. 토큰 파싱 및 검증
    String accessToken;
    Long userId;

    try {
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        log.warn("[회원탈퇴] 유효하지 않은 인증 헤더 형식");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new MessageResponse("유효한 인증 토큰이 필요합니다."));
      }

      accessToken = authHeader.substring(7);

      if (accessToken.isBlank()) {
        log.warn("[회원탈퇴] 빈 액세스 토큰");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new MessageResponse("유효한 액세스 토큰이 필요합니다."));
      }

      // 액세스 토큰 검증
      if (!jwtTokenProvider.validateAccessToken(accessToken)) {
        log.warn("[회원탈퇴] 유효하지 않은 액세스 토큰: {}", maskToken(accessToken));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new MessageResponse("유효하지 않은 액세스 토큰입니다."));
      }

      // 유저 ID 추출
      try {
        userId = tokenService.getUserIdFromToken(accessToken);
        log.info("[회원탈퇴] 사용자 ID: {} 탈퇴 시작", userId);
      } catch (InvalidJwtTokenException | InvalidUserIdFormatException |
               MissingUserIdFromTokenException e) {
        log.warn("[회원탈퇴] 토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new MessageResponse("토큰에서 사용자 정보를 추출할 수 없습니다: " + e.getMessage()));
      }

    } catch (Exception e) {
      log.error("[회원탈퇴] 토큰 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new MessageResponse("인증 처리 중 오류가 발생했습니다."));
    }

    // 2. Redis 작업 - 우선 시도 (DB 작업 전)
    boolean redisSuccess = false;
    try {
      redisSuccess = executeRedisCleanup(userId.toString(), accessToken);
    } catch (Exception e) {
      // Redis 실패는 기록만 하고 계속 진행 (보상 트랜잭션에서 다시 시도)
      log.warn("[회원탈퇴] Redis 작업 실패 (재시도 예정): userId={}, 사유={}", userId, e.getMessage());
    }

    // 3. DB 작업 (사용자 데이터 삭제)
    try {
      userService.deleteUser(userId);
      log.info("[회원탈퇴] DB 작업 성공: userId={}", userId);
    } catch (BusinessException e) {
      log.error("[회원탈퇴] 비즈니스 예외: 코드={}, 메시지={}", e.getErrorCode(), e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new MessageResponse(e.getMessage()));
    } catch (Exception e) {
      log.error("[회원탈퇴] DB 작업 실패: userId={}, 사유={}", userId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new MessageResponse("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage()));
    }

    // 4. Redis 작업이 실패했으면 보상 트랜잭션에서 다시 시도
    if (!redisSuccess) {
      try {
        boolean retrySuccess = retryRedisCleanup(userId.toString(), accessToken);
        log.info("[회원탈퇴] Redis 재시도 결과: userId={}, 성공={}", userId, retrySuccess);
      } catch (Exception e) {
        // Redis 재시도 실패는 로그만 남기고 성공 응답 (DB 작업은 이미 성공)
        log.warn("[회원탈퇴] Redis 재시도 실패 (DB 작업은 성공): userId={}, 사유={}", userId, e.getMessage());
      }
    }

    return ResponseEntity.ok(new MessageResponse("회원 탈퇴가 완료되었습니다."));
  }

  // Redis 작업 실행 메소드 (반환값: 성공 여부)
  private boolean executeRedisCleanup(String userId, String accessToken) {
    boolean refreshTokenDeleted = false;
    boolean accessTokenBlacklisted = false;

    try {
      // Refresh Token 삭제
      redisService.deleteRefreshToken(userId);
      refreshTokenDeleted = true;
      log.debug("[회원탈퇴] 리프레시 토큰 삭제 성공: userId={}", userId);

      // Access Token 블랙리스트 추가
      long remainingTime = jwtTokenProvider.getTokenTimeToLive(accessToken);
      if (remainingTime > 0) {
        redisService.addToBlacklist(accessToken, remainingTime);
        accessTokenBlacklisted = true;
        log.debug("[회원탈퇴] 액세스 토큰 블랙리스트 추가 성공: userId={}, 남은시간={}ms", userId, remainingTime);
      } else {
        accessTokenBlacklisted = true; // 이미 만료된 토큰은 블랙리스트에 추가할 필요 없음
        log.debug("[회원탈퇴] 액세스 토큰 이미 만료됨: userId={}", userId);
      }

      return refreshTokenDeleted && accessTokenBlacklisted;
    } catch (Exception e) {
      log.warn(
          "[회원탈퇴] Redis 작업 실패 상세: userId={}, refreshTokenDeleted={}, accessTokenBlacklisted={}, 사유={}",
          userId, refreshTokenDeleted, accessTokenBlacklisted, e.getMessage());
      throw e; // 상위 메서드에서 처리
    }
  }

  // 보상 트랜잭션 - Redis 작업 재시도
  @Transactional(noRollbackFor = Exception.class) // 예외가 발생해도 롤백하지 않음
  public boolean retryRedisCleanup(String userId, String accessToken) {
    try {
      return executeRedisCleanup(userId, accessToken);
    } catch (Exception e) {
      log.warn("[회원탈퇴] Redis 보상 트랜잭션 실패: userId={}, 사유={}", userId, e.getMessage());
      return false;
    }
  }

  // 토큰 마스킹 (로그 보안)
  private String maskToken(String token) {
    if (token == null || token.length() < 10) {
      return "[too short to mask]";
    }
    return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
  }
}