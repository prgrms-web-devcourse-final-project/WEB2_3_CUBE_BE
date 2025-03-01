package com.roome.domain.user.dto.response;

import com.roome.domain.user.dto.RecommendedUserDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserProfileResponse {
    private String id;
    private String nickname;
    private String profileImage;
    private String bio;
    private List<String> musicGenres;
    private List<String> bookGenres;
    private List<RecommendedUserDto> recommendedUsers;
    private boolean isMyProfile;
    private boolean isFollowing;
}
