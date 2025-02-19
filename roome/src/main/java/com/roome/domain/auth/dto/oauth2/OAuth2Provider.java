package com.roome.domain.auth.dto.oauth2;

import com.roome.domain.auth.exception.InvalidProviderException;
import com.roome.domain.auth.service.OAuth2Factory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Getter
public enum OAuth2Provider {
    GOOGLE("google", "https://oauth2.googleapis.com/token", "https://www.googleapis.com/oauth2/v3/userinfo"),
    KAKAO("kakao", "https://kauth.kakao.com/oauth/token", "https://kapi.kakao.com/v2/user/me"),
    NAVER("naver", "https://nid.naver.com/oauth2.0/token", "https://openapi.naver.com/v1/nid/me");

    private final String registrationId;
    private final String tokenUri;
    private final String userInfoUri;

    @Value("${oauth2.google.client-id}") private String googleClientId;
    @Value("${oauth2.google.client-secret}") private String googleClientSecret;
    @Value("${oauth2.google.redirect-uri}") private String googleRedirectUri;

    OAuth2Provider(String registrationId, String tokenUri, String userInfoUri) {
        this.registrationId = registrationId;
        this.tokenUri = tokenUri;
        this.userInfoUri = userInfoUri;
    }

    public static OAuth2Provider from(String provider) {
        for (OAuth2Provider p : values()) {
            if (p.registrationId.equalsIgnoreCase(provider)) return p;
        }
        throw new InvalidProviderException();
    }

    public String getUserInfoUri(String accessToken) {
        return userInfoUri + "?access_token=" + accessToken;
    }

    public OAuth2Response createOAuth2Response(Map<String, Object> attributes) {
        return OAuth2Factory.getProvider(this.registrationId, attributes);
    }
}