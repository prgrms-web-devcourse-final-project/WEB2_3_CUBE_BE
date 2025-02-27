package com.roome.global.jwt.controller;

import com.roome.domain.auth.dto.response.MessageResponse;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.dto.TokenReissueRequest;
import com.roome.global.jwt.dto.TokenResponse;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "토큰", description = "토큰 관련 API")
public class ReissueController {

  private final TokenService tokenService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(
      summary = "토큰 재발급",
      description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
      @ApiResponse(responseCode = "400", description = "리프레시 토큰이 없거나 유효하지 않음"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "503", description = "데이터베이스 접근 오류"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/reissue-token")
  public ResponseEntity<?> reissueToken(@RequestBody TokenReissueRequest request) {
    try {
      String refreshToken = request.getRefreshToken();

      // 리프레시 토큰 검증
      if (refreshToken == null || refreshToken.isBlank()) {
        return ResponseEntity.badRequest()
            .body(new MessageResponse("리프레시 토큰이 필요합니다."));
      }

      // 리프레시 토큰이 유효한지 확인
      if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
        return ResponseEntity.badRequest()
            .body(new MessageResponse("유효하지 않은 리프레시 토큰입니다."));
      }

      // 새로운 토큰 발급
      JwtToken newToken = tokenService.reissueToken(refreshToken);
      return ResponseEntity.ok(createTokenResponse(newToken));

    } catch (UserNotFoundException e) {
      log.warn("사용자를 찾을 수 없음: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new MessageResponse("해당 리프레시 토큰의 사용자를 찾을 수 없습니다."));
    } catch (DataAccessException e) {
      log.error("데이터 액세스 오류: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(new MessageResponse("데이터베이스 접근 중 오류가 발생했습니다."));
    } catch (Exception e) {
      log.error("토큰 재발급 중 예상치 못한 오류 발생: ", e);
      return ResponseEntity.internalServerError()
          .body(new MessageResponse("토큰 재발급 중 오류가 발생했습니다."));
    }
  }

  private TokenResponse createTokenResponse(JwtToken token) {
    return TokenResponse.builder()
        .accessToken(token.getAccessToken())
        .refreshToken(token.getRefreshToken())
        .tokenType(token.getGrantType())
        .expiresIn(jwtTokenProvider.getAccessTokenExpirationTime() / 1000)
        .build();
  }
}