package com.roome.domain.auth.dto.oauth2;

public interface OAuth2Response {

    OAuth2Provider getProvider();
    String getProviderId();
    String getName();
    String getProfileImageUrl();

}
