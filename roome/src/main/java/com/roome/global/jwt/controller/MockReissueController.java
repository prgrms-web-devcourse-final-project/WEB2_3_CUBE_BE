package com.roome.global.jwt.controller;

import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.helper.TokenResponseHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mock/auth")
@RequiredArgsConstructor
@Tag(name = "Token", description = "토큰 재발급")
public class MockReissueController {

    private final TokenResponseHelper tokenResponseHelper;

    @PostMapping("/reissue-token")
    public ResponseEntity<?> mockReissueToken(
            @CookieValue(value = "refresh_token", defaultValue = "mock_refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        log.info("[Mock 토큰 재발급] Refresh Token: {}", refreshToken);

        // Mock JWT 토큰 생성
        JwtToken newToken = new JwtToken(
                "mock_access_token",
                "mock_refresh_token",
                "Bearer"
        );

        // 토큰을 쿠키와 헤더에 설정
        tokenResponseHelper.setTokenResponse(response, newToken);

        return ResponseEntity.ok(createMockTokenResponse(newToken));
    }

    private Map<String, Object> createMockTokenResponse(JwtToken token) {
        return Map.of(
                "access_token", token.getAccessToken(),
                "token_type", token.getGrantType(),
                "expires_in", 3600,
                "message", "Mock 토큰이 재발급되었습니다."
        );
    }
}
