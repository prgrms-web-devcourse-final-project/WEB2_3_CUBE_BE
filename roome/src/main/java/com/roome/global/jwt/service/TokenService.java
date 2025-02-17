package com.roome.global.jwt.service;

import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.dto.JwtToken;
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
        verifyRefreshTokenMatch(refreshToken, user);

        // 단순히 사용자 ID만 포함
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getProviderId(),
                null,
                Collections.emptyList()  // 권한 없이 빈 리스트로 설정
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
        String userId = claims.getSubject();

        return userRepository.findByProviderId(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private void verifyRefreshTokenMatch(String refreshToken, User user) {
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidRefreshTokenException();
        }
    }
}
