package com.roome.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseRoomThemeResponseDto {
    private Long roomId;
    private String purchasedTheme;
    private int remainingPoints; // 구매 후 남은 포인트
}
