package com.roome.domain.auth.controller;

import com.roome.domain.auth.dto.response.MessageResponse;
import com.roome.domain.user.service.UserService;
import com.roome.global.exception.BusinessException;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import com.roome.global.service.RedisService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final TokenResponseHelper tokenResponseHelper;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final UserService userService;
    private final RedisService redisService;

    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response
    ) {
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

            tokenResponseHelper.removeTokenResponse(response);
            return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<MessageResponse> withdraw(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        try {
            String accessToken = authHeader.substring(7);

            if (accessToken.isBlank() || !jwtTokenProvider.validateAccessToken(accessToken)) {
                // 리프레시 토큰 검증
                if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new MessageResponse("리프레시 토큰이 없거나 유효하지 않습니다."));
                }

                // 유효하지 않은 액세스 토큰을 사용한 경우 새로운 액세스 토큰 발급
                JwtToken newToken = tokenService.reissueToken(refreshToken);
                accessToken = newToken.getAccessToken();
            }

            // 유저 ID 추출 및 회원 탈퇴 처리
            Long userId = tokenService.getUserIdFromToken(accessToken);
            userService.deleteUser(userId);

            // Refresh Token 삭제
            redisService.deleteRefreshToken(userId.toString());

            // Access Token 블랙리스트 추가
            redisService.addToBlacklist(accessToken, jwtTokenProvider.getAccessTokenExpirationTime());

            // 클라이언트 측 토큰 삭제
            tokenResponseHelper.removeTokenResponse(response);
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
