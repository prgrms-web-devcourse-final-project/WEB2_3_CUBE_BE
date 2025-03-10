package com.roome.global.jwt.controller;

import com.roome.domain.auth.dto.response.MessageResponse;
import com.roome.global.jwt.dto.TokenReissueRequest;
import com.roome.global.jwt.dto.TokenResponse;
import com.roome.global.jwt.exception.InvalidJwtTokenException;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "토큰 재발급 API", description = "토큰 재발급 관련 API")
public class ReissueController {

  private final TokenService tokenService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
      @ApiResponse(responseCode = "400", description = "리프레시 토큰이 없거나 유효하지 않음"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "503", description = "데이터베이스 접근 오류"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")})
  @PostMapping("/reissue-token")
  public ResponseEntity<?> reissueToken(
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      @RequestBody(required = false) TokenReissueRequest request,
      HttpServletRequest servletRequest) {

    try {
      // 1. 요청 객체 검증
      if (request == null) {
        log.warn("요청 객체가 null입니다.");
        return ResponseEntity.badRequest().body(new MessageResponse("요청이 올바르지 않습니다. 다시 로그인해 주세요."));
      }

      // 2. 리프레시 토큰 추출
      String refreshToken = request.getRefreshToken();
      log.debug("요청에서 받은 리프레시 토큰: {}", refreshToken != null ? "있음" : "없음");

      // 3. 요청 바디에 리프레시 토큰이 없는 경우 쿠키에서 확인
      if (refreshToken == null || refreshToken.isBlank()) {
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
          for (Cookie cookie : cookies) {
            if ("refresh_token".equals(cookie.getName())) {
              refreshToken = cookie.getValue();
              log.debug("쿠키에서 리프레시 토큰 발견");
              break;
            }
          }
        }
      }

      // 4. 여전히 리프레시 토큰이 없는 경우
      if (refreshToken == null || refreshToken.isBlank()) {
        log.warn("리프레시 토큰이 없음 (바디와 쿠키 모두 확인)");
        return ResponseEntity.badRequest()
            .body(new MessageResponse("Refresh 토큰이 유효하지 않거나, 입력값이 비어 있습니다."));
      }

      // 5. 리프레시 토큰 검증
      if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
        log.warn("유효하지 않은 리프레시 토큰");
        return ResponseEntity.badRequest()
            .body(new MessageResponse("Refresh 토큰이 유효하지 않거나, 입력값이 비어 있습니다."));
      }

      // 6. 현재 액세스 토큰 확인 및 검증
      String currentAccessToken = extractAccessToken(authorizationHeader);
      TokenValidationResult validation = validateCurrentAccessToken(currentAccessToken);

      // 7. 액세스 토큰이 유효하고 만료까지 5분 이상 남은 경우
      final long FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000;
      if (validation.valid && validation.remainingTime > FIVE_MINUTES_IN_MILLIS) {
        log.info("액세스 토큰이 아직 유효함 (남은 시간: {}ms)", validation.remainingTime);
        return ResponseEntity.badRequest()
            .body(new MessageResponse("액세스 토큰이 아직 유효합니다. 만료 임박 시 재요청하세요."));
      }

      // 8. 액세스 토큰 재발급
      String accessToken = tokenService.reissueAccessToken(refreshToken);

      // 9. 성공 응답 반환
      return ResponseEntity.ok(new TokenResponse(accessToken, "Bearer",
          jwtTokenProvider.getAccessTokenExpirationTime() / 1000 // 초 단위로 변환
      ));

    } catch (InvalidRefreshTokenException e) {
      log.warn("리프레시 토큰이 유효하지 않음: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new MessageResponse("Refresh 토큰이 유효하지 않거나, 입력값이 비어 있습니다."));
    } catch (UserNotFoundException e) {
      log.warn("사용자를 찾을 수 없음: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new MessageResponse("해당 리프레시 토큰의 사용자를 찾을 수 없습니다."));
    } catch (DataAccessException e) {
      log.error("데이터 액세스 오류: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(new MessageResponse("데이터베이스 접근 중 오류가 발생했습니다."));
    } catch (Exception e) {
      String errorMsg = String.format("토큰 재발급 중 오류 발생: %s", e.getMessage());
      log.error(errorMsg, e);
      return ResponseEntity.internalServerError()
          .body(new MessageResponse("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
  }

  // 액세스 토큰 추출
  private String extractAccessToken(String authorizationHeader) {
    if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    return null;
  }

  // 액세스 토큰 검증 결과 클래스
  private static class TokenValidationResult {

    final boolean valid;
    final long remainingTime;

    TokenValidationResult(boolean valid, long remainingTime) {
      this.valid = valid;
      this.remainingTime = remainingTime;
    }
  }

  // 현재 액세스 토큰 검증
  private TokenValidationResult validateCurrentAccessToken(String accessToken) {
    if (accessToken == null) {
      return new TokenValidationResult(false, 0);
    }

    boolean valid = false;
    long remainingTime = 0;

    try {
      valid = jwtTokenProvider.validateAccessToken(accessToken);
      if (valid) {
        remainingTime = jwtTokenProvider.getTokenTimeToLive(accessToken);
      }
    } catch (InvalidJwtTokenException e) {
      log.info("액세스 토큰이 만료되었습니다. 새 토큰을 발급합니다.");
    } catch (Exception e) {
      log.warn("액세스 토큰 검증 중 오류 발생: {}", e.getMessage());
    }

    return new TokenValidationResult(valid, remainingTime);
  }
}