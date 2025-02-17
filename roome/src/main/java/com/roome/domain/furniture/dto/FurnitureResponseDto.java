package com.roome.domain.furniture.dto;

import com.roome.domain.furniture.entity.Furniture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FurnitureResponseDto {
    private String furnitureType;
    private Boolean isVisible;
    private Integer level;

    public static FurnitureResponseDto from(Furniture furniture) {
        return FurnitureResponseDto.builder()
                .furnitureType(furniture.getFurnitureType().name())
                .isVisible(furniture.getIsVisible())
                .level(furniture.getLevel())
                .build();
    }
}
