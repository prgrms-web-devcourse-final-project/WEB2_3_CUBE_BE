package com.roome.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateRoomThemeResponseDto {
    private Long roomId;
    private String updatedTheme;
}
