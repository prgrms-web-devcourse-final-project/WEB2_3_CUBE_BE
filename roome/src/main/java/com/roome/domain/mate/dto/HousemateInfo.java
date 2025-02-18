package com.roome.domain.mate.dto;

import com.roome.domain.user.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HousemateInfo {
    private Long userId;
    private String nickname;
    private String profileImage;
    private String bio;
    private Status status;

    @Builder
    public HousemateInfo(Long userId, String nickname, String profileImage, String bio, Status status) {
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.bio = bio;
        this.status = status;
    }
}