package com.roome.domain.oauth2.dto;

import com.roome.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class OAuth2UserPrincipal implements OAuth2User, UserDetails {
    private final User user;
    private final OAuth2Response oAuth2Response;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public OAuth2UserPrincipal(User user, OAuth2Response oAuth2Response) {
        this.user = user;
        this.oAuth2Response = oAuth2Response;
        this.authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        this.attributes = Collections.singletonMap("user", oAuth2Response);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getProviderId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}