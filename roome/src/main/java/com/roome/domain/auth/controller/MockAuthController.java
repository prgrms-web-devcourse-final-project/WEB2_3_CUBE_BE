package com.roome.domain.auth.controller;

import com.roome.domain.auth.dto.request.LoginRequest;
import com.roome.domain.auth.dto.response.LoginResponse;
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
@Tag(name = "Auth", description = "로그인/로그아웃/탈퇴")
public class MockAuthController {

    private final TokenResponseHelper tokenResponseHelper;

    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> mockLogin(
            @PathVariable String provider,
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        log.info("[Mock 로그인] Provider: {}", provider);

        JwtToken jwtToken = JwtToken.builder()
                .accessToken("mock_access_token_" + provider)
                .refreshToken("mock_refresh_token_" + provider)
                .grantType("Bearer")
                .build();

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(999L)
                .nickname("Mock User")
                .email("mockuser@example.com")
                .profileImage("https://mock-image.com/profile.png")
                .roomId(999L)
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .expiresIn(3600L)
                .user(userInfo)
                .build();

        tokenResponseHelper.setTokenResponse(response, jwtToken);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> mockLogout(HttpServletResponse response) {
        log.info("[Mock 로그아웃] 로그아웃 처리됨");

        tokenResponseHelper.removeTokenResponse(response);

        return ResponseEntity.ok("로그아웃 성공 (Mock)");
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Map<String, String>> mockWithdraw(@RequestHeader("Authorization") String authorizationHeader) {

        //Bearer mock_access_token_{provider}
        // mock_access_token_{provider}
        log.info("[Mock 회원 탈퇴] 요청 Authorization: {}", authorizationHeader);

        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "error",
                    "message", "유효하지 않은 토큰입니다."
            ));
        }

        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }
}
