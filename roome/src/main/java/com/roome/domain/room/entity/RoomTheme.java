package com.roome.domain.room.entity;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum RoomTheme {
    BASIC("basic"),
    FOREST("forest"),
    MARINE("marine");

    private final String themeName;

    RoomTheme(String themeName) {
        this.themeName = themeName;
    }

    public static RoomTheme fromString(String theme) {
        for (RoomTheme t : values()) {
            if (t.themeName.equalsIgnoreCase(theme)) {
                return t;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_ROOM_THEME);
    }
}
