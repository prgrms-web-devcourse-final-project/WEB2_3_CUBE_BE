package com.roome.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookGenre {
    SF("SF"),
    MYSTERY("추리"),
    LIFE_SCIENCE("생명과학");

    private final String genre;
}