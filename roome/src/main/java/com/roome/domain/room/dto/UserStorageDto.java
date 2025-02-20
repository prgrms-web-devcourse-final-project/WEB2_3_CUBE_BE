package com.roome.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserStorageDto {

    private Long savedMusic;
    private Long savedBooks;
    private Long writtenReviews;
    private Long writtenMusicLogs;

    public static UserStorageDto from(Long savedMusic, Long savedBooks, Long writtenReviews, Long writtenMusicLogs) {
        return UserStorageDto.builder()
                .savedMusic(savedMusic)
                .savedBooks(savedBooks)
                .writtenReviews(writtenReviews)
                .writtenMusicLogs(writtenMusicLogs)
                .build();
    }

}
