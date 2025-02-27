package com.roome.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecommendedUserDto {
    Long userId;
    String nickname;
    String profileImage;
}
