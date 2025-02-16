package com.roome.global.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("로그인 실패: {}, 요청 경로: {}", exception.getMessage(), request.getRequestURI(), exception);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String errorType = "인증 실패";
        String errorMessage = exception.getMessage();

        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauthException = (OAuth2AuthenticationException) exception;
            errorType = "OAuth2 인증 오류";
            errorMessage = "소셜 로그인 인증 중 오류가 발생했습니다: " + oauthException.getError().getErrorCode();
        } else if (exception instanceof BadCredentialsException) {
            errorType = "잘못된 인증 정보";
            errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
        } else if (exception instanceof DisabledException) {
            errorType = "비활성화된 계정";
            errorMessage = "해당 계정이 비활성화되었습니다.";
        }

        response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                        Map.of(
                                "error", errorType,
                                "message", errorMessage,
                                "timestamp", System.currentTimeMillis(),
                                "path", request.getRequestURI()
                        )
                )
        );
    }
}
