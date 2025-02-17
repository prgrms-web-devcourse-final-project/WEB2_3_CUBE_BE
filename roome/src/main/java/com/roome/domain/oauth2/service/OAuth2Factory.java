package com.roome.domain.oauth2.service;

import com.roome.domain.oauth2.dto.*;
import com.roome.domain.oauth2.exception.UnsupportedOAuth2ProviderException;

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
            throw new UnsupportedOAuth2ProviderException();
        }
    }
}

