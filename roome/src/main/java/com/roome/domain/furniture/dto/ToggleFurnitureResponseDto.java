package com.roome.domain.furniture.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleFurnitureResponseDto {
    private Long roomId;
    private FurnitureResponseDto furniture;
}
