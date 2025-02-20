package com.roome.domain.room.dto;

import com.roome.domain.room.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StorageLimitsDto {
    private int maxMusic;
    private int maxBooks;

    public static StorageLimitsDto from(Room room) {
        return StorageLimitsDto.builder()
                .maxMusic(room.getMaxMusic())
                .maxBooks(room.getMaxBooks())
                .build();
    }
}
