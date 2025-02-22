package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.*;
import com.roome.domain.auth.exception.InvalidProviderException;

import java.util.Map;

public class OAuth2Factory {
    public static OAuth2Response createResponse(OAuth2Provider provider, Map<String, Object> attributes) {
        switch (provider) {
            case GOOGLE:
                return new GoogleResponse(attributes);
            case NAVER:
                return new NaverResponse(attributes);
            case KAKAO:
                return new KakaoResponse(attributes);
            default:
                throw new InvalidProviderException();
        }
    }
}
