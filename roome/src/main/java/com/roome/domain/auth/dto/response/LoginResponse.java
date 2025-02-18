package com.roome.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private Long userId;
        private String nickname;
        private String email;
        private Long roomId;
        private String profileImage;
    }
}
