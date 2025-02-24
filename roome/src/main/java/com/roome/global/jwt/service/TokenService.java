package com.roome.global.jwt.service;

import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.exception.InvalidUserIdFormatException;
import com.roome.global.jwt.exception.MissingUserIdFromTokenException;
import com.roome.global.jwt.exception.UserNotFoundException;
import com.roome.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisService redisService;

    public JwtToken reissueToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        User user = findUserByRefreshToken(refreshToken);
        String userId = user.getId().toString();

        // Redis에 저장된 Refresh Token과 비교
        String savedRefreshToken = redisService.getRefreshToken(userId);
        if (!refreshToken.equals(savedRefreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        // 새 토큰 발급
        JwtToken newToken = jwtTokenProvider.createToken(userId);

        // 새 Refresh Token을 Redis에 저장
        redisService.saveRefreshToken(userId, newToken.getRefreshToken(),
                jwtTokenProvider.getRefreshTokenExpirationTime());

        return newToken;
    }

    private User findUserByRefreshToken(String refreshToken) {
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        if (userId == null || userId.isBlank()) {
            throw new MissingUserIdFromTokenException();
        }

        try {
            return userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(UserNotFoundException::new);
        } catch (NumberFormatException e) {
            throw new InvalidUserIdFormatException();
        }
    }


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