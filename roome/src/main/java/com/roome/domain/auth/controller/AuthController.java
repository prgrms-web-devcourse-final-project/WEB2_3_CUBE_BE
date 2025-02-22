package com.roome.domain.auth.controller;

import com.roome.domain.auth.dto.oauth2.OAuth2Provider;
import com.roome.domain.auth.dto.request.LoginRequest;
import com.roome.domain.auth.dto.response.LoginResponse;
import com.roome.domain.auth.dto.response.MessageResponse;
import com.roome.domain.auth.exception.InvalidProviderException;
import com.roome.domain.auth.exception.MissingAuthorizationCodeException;
import com.roome.domain.auth.exception.OAuth2AuthenticationProcessingException;
import com.roome.domain.auth.service.OAuth2LoginService;
import com.roome.domain.user.service.UserService;
import com.roome.global.exception.BusinessException;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.helper.TokenResponseHelper;
import com.roome.global.jwt.service.JwtTokenProvider;
import com.roome.global.jwt.service.TokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final OAuth2LoginService oAuth2LoginService;
    private final TokenResponseHelper tokenResponseHelper;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final UserService userService;

    @SecurityRequirements
    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> login(
            @PathVariable("provider") String provider,
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {

            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                log.error("[로그인 실패] 요청에 authorization code가 없음 (provider: {})", provider);
                throw new MissingAuthorizationCodeException();
            }

            OAuth2Provider oAuth2Provider = OAuth2Provider.from(provider);

            // 로그인 처리
            LoginResponse loginResponse = oAuth2LoginService.login(oAuth2Provider, request.getCode());

            if (loginResponse == null) {
                log.error("[로그인 실패] 로그인 응답이 null임 (provider: {})", provider);
                throw new OAuth2AuthenticationProcessingException();
            }

            // JWT 발급 후 응답 헤더와 쿠키 설정
            tokenResponseHelper.setTokenResponse(response, new JwtToken(
                    loginResponse.getAccessToken(),
                    loginResponse.getRefreshToken(),
                    "Bearer"
            ));

            return ResponseEntity.ok(loginResponse);
        } catch (InvalidProviderException e) {
            log.error("[로그인 실패] 잘못된 provider 요청 (provider: {})", provider, e);
            throw e;
        } catch (OAuth2AuthenticationProcessingException e) {
            log.error("[로그인 실패] OAuth2 처리 중 오류 발생 (provider: {})", provider, e);
            throw e;
        } catch (Exception e) {
            log.error("Login failed for provider: {}", provider, e);
            throw new OAuth2AuthenticationProcessingException();
        }
    }

    // TODO: Redis 도입 후 블랙리스트 활용 예정
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response
    ) {
        try {
            String accessToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }

            // 토큰이 유효하지 않아도 로그아웃은 처리
            if (accessToken != null && !accessToken.isBlank()) {
                if (jwtTokenProvider.validateToken(accessToken)) {
                    log.info("유효한 토큰으로 로그아웃: {}", accessToken);
                } else {
                    log.warn("만료된 토큰으로 로그아웃: {}", accessToken);
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
            // Authorization 헤더 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("인증 토큰이 필요합니다."));
            }

            String accessToken = authHeader.substring(7);

            // 액세스 토큰 빈 값 체크
            if (accessToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("유효하지 않은 토큰입니다."));
            }

            // 토큰 검증 및 재발급 처리
            if (!jwtTokenProvider.validateToken(accessToken)) {
                if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new MessageResponse("리프레시 토큰이 없거나 유효하지 않습니다."));
                }

                JwtToken newToken = tokenService.reissueToken(refreshToken);
                accessToken = newToken.getAccessToken();
            }

            // 유저 ID 추출 및 회원 탈퇴 처리
            Long userId = tokenService.getUserIdFromToken(accessToken);
            userService.deleteUser(userId);

            // 토큰 무효화
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
