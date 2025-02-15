package com.roome.domain.oauth2.dto;

public interface OAuth2Response {

    OAuth2Provider getProvider();
    String getProviderId();
    String getName();
    String getProfileImageUrl();

}
