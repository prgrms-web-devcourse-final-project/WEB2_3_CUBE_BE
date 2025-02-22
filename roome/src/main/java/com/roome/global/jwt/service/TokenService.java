package com.roome.global.jwt.service;

import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.dto.JwtToken;
import com.roome.global.jwt.exception.InvalidJwtTokenException;
import com.roome.global.jwt.exception.InvalidRefreshTokenException;
import com.roome.global.jwt.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtToken reissueToken(String refreshToken) {
        validateRefreshToken(refreshToken);
        User user = findUserByRefreshToken(refreshToken);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getId().toString(), null, Collections.emptyList()
        );

        return jwtTokenProvider.createToken(authentication);
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Refresh Token 검증 실패: {}", refreshToken);
            throw new InvalidRefreshTokenException();
        }
    }

    private User findUserByRefreshToken(String refreshToken) {
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());

        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public Long getUserIdFromToken(String accessToken) {
        return Long.valueOf(jwtTokenProvider.getAuthentication(accessToken).getName());
    }

    public Long validateAndGetUserId(String accessToken) {
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new InvalidJwtTokenException();
        }
        return getUserIdFromToken(accessToken);
    }
}