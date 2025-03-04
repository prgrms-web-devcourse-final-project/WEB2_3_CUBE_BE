package com.roome.domain.room.dto;

import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.room.entity.Room;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RoomResponseDto {

    private Long roomId;
    private Long userId;
    private String theme;
    private String nickname;
    private LocalDateTime createdAt;
    private List<FurnitureResponseDto> furnitures;
    private StorageLimitsDto storageLimits;
    private UserStorageDto userStorage;
    private List<String> topBookGenres;
    private List<String> topCdGenres;

    public static RoomResponseDto from(Room room, Long savedMusic, Long savedBooks, Long writtenReviews, Long writtenMusicLogs) {
        return RoomResponseDto.builder()
                .roomId(room.getId())
                .userId(room.getUser().getId())
                .theme(room.getTheme().getThemeName())
                .nickname(room.getUser().getNickname())
                .createdAt(room.getCreatedAt())
                .furnitures(room.getFurnitures() != null
                        ? room.getFurnitures().stream().map(FurnitureResponseDto::from).collect(Collectors.toList())
                        : Collections.emptyList())
                .storageLimits(StorageLimitsDto.from(room))
                .userStorage(UserStorageDto.from(savedMusic, savedBooks, writtenReviews, writtenMusicLogs))
                .build();
    }

    public static RoomResponseDto from(Room room, Long savedMusic, Long savedBooks,
                                       Long writtenReviews, Long writtenMusicLogs,
                                       List<String> topBookGenres, List<String> topCdGenres) {
        return RoomResponseDto.builder()
                .roomId(room.getId())
                .userId(room.getUser().getId())
                .theme(room.getTheme().name())
                .nickname(room.getUser().getNickname())
                .createdAt(room.getCreatedAt())
                .furnitures(room.getFurnitures() != null
                        ? room.getFurnitures().stream().map(FurnitureResponseDto::from).collect(Collectors.toList())
                        : Collections.emptyList())
                .storageLimits(StorageLimitsDto.from(room))
                .userStorage(UserStorageDto.from(savedMusic, savedBooks, writtenReviews, writtenMusicLogs))
                .topBookGenres(topBookGenres)
                .topCdGenres(topCdGenres)
                .build();
    }
}
