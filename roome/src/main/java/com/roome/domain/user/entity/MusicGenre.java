package com.roome.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MusicGenre {
    HIPHOP("힙합"),
    LOFI("로파이"),
    AFRO("아프로");

    private final String genre;
}
