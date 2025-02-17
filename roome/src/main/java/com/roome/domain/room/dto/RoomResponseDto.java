package com.roome.domain.room.dto;

import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.room.entity.Room;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RoomResponseDto {

    private Long roomId;
    private Long userId;
    private String theme;
    private LocalDateTime createdAt;
    private List<FurnitureResponseDto> furnitures;

    public static RoomResponseDto from(Room room) {
        return RoomResponseDto.builder()
                .roomId(room.getId())
                .userId(room.getUserId())
                .theme(room.getTheme())
                .createdAt(room.getCreatedAt())
                .furnitures(room.getFurnitures().stream()
                        .map(FurnitureResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
