package com.roome.domain.userGenrePreference.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenreType {
    CD("CD"),
    BOOK("Book");

    private final String type;
}