package com.roome.domain.rank.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MockRankResponse {
    private List<RankInfo> ranking;
    private String message;
    private String updateTime;

    @Getter
    @Builder
    public static class RankInfo {
        private int rank;
        private String userId;
        private String nickname;
        private String profileImage;
        private int score;
    }
}
