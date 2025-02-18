package com.roome.domain.auth.dto.oauth2;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;

        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (this.kakaoAccount == null) {
            throw new OAuth2AuthenticationException("카카오 계정 정보가 없습니다.");
        }

        this.profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (this.profile == null) {
            throw new OAuth2AuthenticationException("카카오 프로필 정보가 없습니다.");
        }
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.KAKAO;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        if (id == null) {
            throw new OAuth2AuthenticationException("카카오 ID가 없습니다.");
        }
        return id.toString();
    }

    @Override
    public String getName() {
        Object nickname = profile.get("nickname");
        return nickname != null ? nickname.toString() : "Unknown";
    }

    @Override
    public String getProfileImageUrl() {
        Object profileImage = profile.get("profile_image_url");
        return profileImage != null ? profileImage.toString() : null;
    }
}
