package com.roome.global.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.auth.exception.AuthenticationFailedException;
import com.roome.domain.auth.exception.DisabledAccountException;
import com.roome.domain.auth.exception.InvalidLoginException;
import com.roome.domain.auth.exception.OAuth2AuthenticationProcessingException;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.error("로그인 실패: {}, 요청 경로: {}", exception.getMessage(), request.getRequestURI(), exception);

        try {
            BusinessException businessException = convertToBusinessException(exception);

            response.setStatus(businessException.getErrorCode().getStatus().value());
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");

            ErrorResponse errorResponse = new ErrorResponse(
                    businessException.getErrorCode().getMessage(),
                    businessException.getErrorCode().getStatus().toString()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        } catch (Exception e) {
            log.error("예외 처리 중 오류 발생", e);
            throw e;
        }
    }

    // 커스텀 예외
    private BusinessException convertToBusinessException(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException) {
            return new OAuth2AuthenticationProcessingException();
        } else if (exception instanceof BadCredentialsException) {
            return new InvalidLoginException();
        } else if (exception instanceof DisabledException) {
            return new DisabledAccountException();
        } else {
            return new AuthenticationFailedException();
        }
    }
}