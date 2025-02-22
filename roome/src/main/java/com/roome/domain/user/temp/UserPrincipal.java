package com.roome.domain.user.temp;

import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String profileImage;
    private String bio;
    private Provider provider;
    private String providerId;
    private Status status;
    private String refreshToken;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.bio = user.getBio();
        this.provider = user.getProvider();
        this.providerId = user.getProviderId();
        this.status = user.getStatus();
        this.refreshToken = user.getRefreshToken();
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getBio() {
        return bio;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public Status getStatus() {
        return status;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 필요한 경우 권한 정보 추가
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인이므로 패스워드 불필요
    }

    @Override
    public String getUsername() {
        return email; // 이메일을 username으로 사용
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
        return Status.ONLINE.equals(status);
    }
}
