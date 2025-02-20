package com.roome.domain.room.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserStorageDto {

    private int savedMusic;
    private int savedBooks;
    private int writtenReviews;
    private int writtenMusicLogs;

    public static UserStorageDto from(int savedMusic, int savedBooks, int writtenReviews, int writtenMusicLogs) {
        return UserStorageDto.builder()
                .savedMusic(savedMusic)
                .savedBooks(savedBooks)
                .writtenReviews(writtenReviews)
                .writtenMusicLogs(writtenMusicLogs)
                .build();
    }

}
