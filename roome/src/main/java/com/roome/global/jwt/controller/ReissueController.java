package com.roome.global.jwt.controller;

import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ReissueController {

    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenResponseHelper tokenResponseHelper;

    @PostMapping("/reissue-token")
    public ResponseEntity<?> reissueToken(
            @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response
    ) {

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        JwtToken newToken = tokenService.reissueToken(refreshToken);
        tokenResponseHelper.setTokenResponse(response, newToken);

        return ResponseEntity.ok(createTokenResponse(newToken));
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