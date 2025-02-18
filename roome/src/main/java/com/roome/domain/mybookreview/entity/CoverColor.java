package com.roome.domain.mybookreview.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CoverColor {

    BLUE("파랑"),
    RED("빨강"),
    GREEN("초록");

    private final String description;
}
