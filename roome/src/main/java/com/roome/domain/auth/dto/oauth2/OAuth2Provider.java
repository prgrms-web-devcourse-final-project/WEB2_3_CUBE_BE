package com.roome.domain.auth.dto.oauth2;

import com.roome.domain.auth.exception.InvalidProviderException;
import com.roome.domain.auth.service.OAuth2Factory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum OAuth2Provider {
    GOOGLE(
            "google",
            "https://oauth2.googleapis.com/token",
            "https://www.googleapis.com/oauth2/v3/userinfo",
            true
    ),
    KAKAO(
            "kakao",
            "https://kauth.kakao.com/oauth/token",
            "https://kapi.kakao.com/v2/user/me",
            false
    ),
    NAVER(
            "naver",
            "https://nid.naver.com/oauth2.0/token",
            "https://openapi.naver.com/v1/nid/me",
            false
    );

    private final String registrationId;
    private final String tokenUri;
    private final String userInfoUri;
    private final boolean requiresDecoding;

    public static OAuth2Provider from(String provider) {
        String cleanedProvider = provider.trim().toLowerCase();
        for (OAuth2Provider p : values()) {
            if (p.registrationId.equals(cleanedProvider)) return p;
        }
        throw new InvalidProviderException();
    }

    public OAuth2Response createOAuth2Response(Map<String, Object> attributes) {
        return OAuth2Factory.createResponse(this, attributes);
    }

    public boolean requiresDecoding() {
        return requiresDecoding;
    }
}