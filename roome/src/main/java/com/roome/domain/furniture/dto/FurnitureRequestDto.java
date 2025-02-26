package com.roome.domain.furniture.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FurnitureRequestDto {
    private String furnitureType;
}
