package com.roome.domain.furniture.dto;

import com.roome.domain.furniture.entity.Furniture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FurnitureResponseDto {
    private String furnitureType;
    private Boolean isVisible;
    private int level;
    private int maxCapacity;
    private List<String> topGenres;

    public static FurnitureResponseDto from(Furniture furniture) {
        return FurnitureResponseDto.builder()
                .furnitureType(furniture.getFurnitureType().name())
                .isVisible(furniture.getIsVisible())
                .level(furniture.getLevel())
                .maxCapacity(furniture.getMaxCapacity())
                .build();
    }

    public static FurnitureResponseDto from(Furniture furniture, List<String> topGenres) {
        return FurnitureResponseDto.builder()
                .furnitureType(furniture.getFurnitureType().name())
                .isVisible(furniture.getIsVisible())
                .level(furniture.getLevel())
                .maxCapacity(furniture.getMaxCapacity())
                .topGenres(topGenres)
                .build();
    }
}
