package com.roome.global.auth;

import com.roome.global.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthenticatedUserArgumentResolverTest {

  private AuthenticatedUserArgumentResolver resolver;
  private MethodParameter methodParameter;
  private NativeWebRequest webRequest;
  private ModelAndViewContainer mavContainer;

  @BeforeEach
  void setUp() throws NoSuchMethodException {
    resolver = new AuthenticatedUserArgumentResolver();
    webRequest = mock(NativeWebRequest.class);
    mavContainer = mock(ModelAndViewContainer.class);

    methodParameter = mock(MethodParameter.class);
    when(methodParameter.hasParameterAnnotation(AuthenticatedUser.class)).thenReturn(true);
    when(methodParameter.getParameterType()).thenAnswer(invocation -> Long.class);
  }

  @Test
  @DisplayName("Given 로그인한 사용자 When ArgumentResolver가 실행될 때 Then userId가 반환된다.")
  void resolveArgument_Success() throws Exception {
    // Given
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("123"); // userId 설정

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    // When
    Object resolvedArgument = resolver.resolveArgument(methodParameter, mavContainer, webRequest, null);

    // Then
    assertThat(resolvedArgument).isEqualTo(123L);
  }

  @Test
  @DisplayName("Given 로그인하지 않은 사용자 When ArgumentResolver 실행 Then UnauthorizedException 발생")
  void resolveArgument_NotAuthenticated_ThrowsException() {
    // Given
    SecurityContextHolder.clearContext(); // 로그인 안 된 상태

    // When & Then
    assertThatThrownBy(() -> resolver.resolveArgument(methodParameter, mavContainer, webRequest, null))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("iven 인증되지 않은 사용자 When ArgumentResolver 실행 Then UnauthorizedException 발생")
  void resolveArgument_AuthenticationNotValid_ThrowsException() {
    // Given
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThatThrownBy(() -> resolver.resolveArgument(methodParameter, mavContainer, webRequest, null))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  @DisplayName("Given userId가 숫자가 아닐 때 When ArgumentResolver 실행 Then NumberFormatException 발생")
  void resolveArgument_InvalidUserIdFormat_ThrowsException() {
    // Given
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("invalid_user_id"); // 숫자가 아닌 값

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    // When & Then
    assertThatThrownBy(() -> resolver.resolveArgument(methodParameter, mavContainer, webRequest, null))
        .isInstanceOf(NumberFormatException.class);
  }
}
