package com.roome.domain.auth.service;

import com.roome.domain.auth.dto.oauth2.*;
import com.roome.domain.auth.exception.InvalidProviderException;

import java.util.Map;

public class OAuth2Factory {

    public static OAuth2Response getProvider(String registrationId, Map<String, Object> attributes) {
        if (OAuth2Provider.GOOGLE.getRegistrationId().equals(registrationId)) {
            return new GoogleResponse(attributes);
        } else if (OAuth2Provider.NAVER.getRegistrationId().equals(registrationId)) {
            return new NaverResponse(attributes);
        } else if (OAuth2Provider.KAKAO.getRegistrationId().equals(registrationId)) {
            return new KakaoResponse(attributes);
        } else {
            throw new InvalidProviderException();
        }
    }
}

