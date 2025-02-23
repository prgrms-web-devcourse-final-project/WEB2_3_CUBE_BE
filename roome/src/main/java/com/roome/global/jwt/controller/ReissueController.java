package com.roome.global.jwt.controller;

import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ReissueController {

    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenResponseHelper tokenResponseHelper;

    @Operation(security = { @SecurityRequirement(name = "cookieAuth") })
    @PostMapping("/reissue-token")
    public ResponseEntity<?> reissueToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        try {
            // 액세스 토큰이 없는 경우
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "액세스 토큰이 필요합니다."));
            }

            String accessToken = authHeader.substring(7);

            // 액세스 토큰이 아직 유효한 경우
            if (jwtTokenProvider.validateAccessToken(accessToken)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "아직 유효한 액세스 토큰입니다."));
            }

            // 액세스 토큰이 만료된 경우에만 리프레시 토큰 확인
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "리프레시 토큰이 없습니다."));
            }

            // 리프레시 토큰 검증
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "유효하지 않은 리프레시 토큰입니다."));
            }

            // 새로운 토큰 발급
            JwtToken newToken = tokenService.reissueToken(refreshToken);
            tokenResponseHelper.setTokenResponse(response, newToken);

            return ResponseEntity.ok(createTokenResponse(newToken));
        } catch (Exception e) {
            log.error("토큰 재발급 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "토큰 재발급 중 오류가 발생했습니다."));
        }
    }

    private Map<String, Object> createTokenResponse(JwtToken token) {
        return Map.of(
                "access_token", token.getAccessToken(),
                "token_type", token.getGrantType(),
                "expires_in", 3600,
                "message", "토큰이 재발급되었습니다."
        );
    }
}