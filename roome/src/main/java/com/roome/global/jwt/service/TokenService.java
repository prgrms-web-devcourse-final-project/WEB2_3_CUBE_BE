package com.roome.global.jwt.service;

import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.exception.InvalidUserIdFormatException;
import com.roome.global.jwt.exception.MissingUserIdFromTokenException;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final RedisService redisService;

  @Transactional
  public String reissueAccessToken(String refreshToken) {
    if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
      log.warn("유효하지 않은 리프레시 토큰");
      throw new InvalidRefreshTokenException();
    }

    User user = findUserByRefreshToken(refreshToken);
    String userId = user.getId().toString();

    String savedRefreshToken = redisService.getRefreshToken(userId);
    if (savedRefreshToken == null || !refreshToken.equals(savedRefreshToken)) {
      log.warn("Redis에 저장된 토큰이 없거나 일치하지 않음");
      throw new InvalidRefreshTokenException();
    }

    return jwtTokenProvider.createAccessToken(userId);
  }

  private User findUserByRefreshToken(String refreshToken) {
    String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
    return userRepository.findById(Long.valueOf(userId)).orElseThrow(UserNotFoundException::new);
  }

  @Transactional(readOnly = true)
  public Long getUserIdFromToken(String accessToken) {
    String userIdStr = jwtTokenProvider.getUserIdFromToken(accessToken);

    if (userIdStr == null || userIdStr.trim().isEmpty()) {
      throw new MissingUserIdFromTokenException();
    }

    try {
      return Long.valueOf(userIdStr);
    } catch (NumberFormatException e) {
      throw new InvalidUserIdFormatException();
    }
  }
}