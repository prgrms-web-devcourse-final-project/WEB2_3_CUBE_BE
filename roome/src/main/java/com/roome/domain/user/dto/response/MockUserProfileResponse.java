package com.roome.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MockUserProfileResponse {
    private String id;
    private String nickname;
    private String profileImage;
    private String bio;
    private List<String> bookGenres;
    private List<String> musicGenres;
    private List<SimilarUser> similarUser;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SimilarUser {
        private String userId;
        private String nickname;
        private String profileImage;
    }
}
