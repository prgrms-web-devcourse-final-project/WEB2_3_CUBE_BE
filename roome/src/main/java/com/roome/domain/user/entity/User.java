package com.roome.domain.user.entity;

import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(/*nullable = false, */length = 255, unique = true)
    private String email;

    // 소셜에서 가져온 실제 이름
    @Column(nullable = false, length = 10)
    private String name;

    // 서비스에서 사용할 닉네임
    private String nickname;

    @Column(length = 500)
    private String profileImage;

    @Column(length = 30)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Provider provider;

    @Column(length = 255, unique = true, nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    private LocalDateTime lastLogin;

    // TODO: 추후 Redis 사용
    @Column(length = 1000)
    private String refreshToken;

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String profileImage, String bio) {
        boolean updated = false;

        if (nickname != null && !nickname.equals(this.nickname)) {
            this.nickname = nickname;
            updated = true;
        }

        if (profileImage != null && !profileImage.equals(this.profileImage)) {
            this.profileImage = profileImage;
            updated = true;
        }

        if (bio != null && !bio.equals(this.bio)) {
            this.bio = bio;
            updated = true;
        }

        if (updated) {
            this.lastLogin = LocalDateTime.now();
        }
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    // TODO: 추후 Redis 사용
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}