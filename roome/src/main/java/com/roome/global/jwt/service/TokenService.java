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
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        User user = findUserByRefreshToken(refreshToken);

        return jwtTokenProvider.createToken(user.getId().toString());
    }

    private User findUserByRefreshToken(String refreshToken) {
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());

        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public Long getUserIdFromToken(String accessToken) {
        return Long.valueOf(jwtTokenProvider.parseClaims(accessToken).getSubject());
    }
}