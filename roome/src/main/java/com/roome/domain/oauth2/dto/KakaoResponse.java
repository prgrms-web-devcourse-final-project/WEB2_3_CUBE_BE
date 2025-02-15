package com.roome.domain.oauth2.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> properties;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.properties = (Map<String, Object>) attributes.get("properties");
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.KAKAO;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
        return properties.get("nickname").toString();
    }

    @Override
    public String getProfileImageUrl() {
        return attributes.get("profile_image").toString();
    }
}
