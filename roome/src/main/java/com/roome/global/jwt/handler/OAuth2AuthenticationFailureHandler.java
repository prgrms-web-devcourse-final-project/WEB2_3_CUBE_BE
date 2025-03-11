package com.roome.global.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    String errorMessage = exception.getMessage() != null ? exception.getMessage() : "인증 처리 중 오류 발생";
    String errorCode = getErrorCode(exception);

    log.error("OAuth2 로그인 실패 - 공급자: {}, 오류 코드: {}, 메시지: {}", getProvider(request), errorCode,
        errorMessage, exception);

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "OAuth2 인증 실패");
    errorResponse.put("error_code", errorCode);
    errorResponse.put("message", errorMessage);
    errorResponse.put("detailed_message", exception.toString());

    response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
  }

  private String getErrorCode(AuthenticationException exception) {
    if (exception.getMessage().contains("invalid_token")) {
      return "INVALID_TOKEN";
    } else if (exception.getMessage().contains("access_denied")) {
      return "ACCESS_DENIED";
    } else if (exception.getMessage().contains("user_not_found")) {
      return "USER_NOT_FOUND";
    }
    return "AUTHENTICATION_FAILED";
  }

  // provider 정보 로그에 남기기 위한 파싱
  private String getProvider(HttpServletRequest request) {
    String referer = request.getHeader("Referer");
    if (referer != null) {
      if (referer.contains("google")) {
        return "Google";
      }
      if (referer.contains("kakao")) {
        return "Kakao";
      }
      if (referer.contains("naver")) {
        return "Naver";
      }
    }
    return "Unknown";
  }
}
