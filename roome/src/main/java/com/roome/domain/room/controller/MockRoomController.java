package com.roome.domain.room.controller;

import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.entity.FurnitureCapacity;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.UpdateRoomThemeRequestDto;
import com.roome.domain.room.dto.StorageLimitsDto;
import com.roome.domain.room.dto.UserStorageDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mock/rooms")
@RequiredArgsConstructor
@Tag(name = "Room", description = "방 조회/테마 업데이트/가구 활성화 및 비활성화")
public class MockRoomController {
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDto> getRoom(
            @PathVariable Long roomId
    ) {
        RoomResponseDto roomResponseDto = createMockRoomResponse(roomId, "basic");
        return ResponseEntity.ok(roomResponseDto);
    }

    @GetMapping
    public ResponseEntity<RoomResponseDto> getRoomByUserId(
            @RequestParam("userId") Long userId
    ){
        RoomResponseDto roomResponseDto = createMockRoomResponse(1L, "forest");
        return ResponseEntity.ok(roomResponseDto);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> updateRoomTheme(
            @RequestParam("userId") Long userId,
            @PathVariable Long roomId,
            @RequestBody UpdateRoomThemeRequestDto requestDto
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("roomId", roomId);
        response.put("updatedTheme", requestDto.getThemeName());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{roomId}/furniture")
    public ResponseEntity<FurnitureResponseDto> toggleFurnitureVisibility(
            @RequestParam("userId") Long userId,
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request
    ) {
        String furnitureTypeStr = request.get("furnitureType");

        if (furnitureTypeStr == null) {
            return ResponseEntity.badRequest().body(null);
        }

        FurnitureType furnitureType;
        try {
            furnitureType = FurnitureType.valueOf(furnitureTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }

        // 가구 리스트에서 해당 타입 찾기
        List<FurnitureResponseDto> furnitureList = List.of(
                new FurnitureResponseDto("BOOKSHELF", true, 3, FurnitureCapacity.getCapacity(FurnitureType.BOOKSHELF, 3)),
                new FurnitureResponseDto("CD_RACK", false, 1, FurnitureCapacity.getCapacity(FurnitureType.CD_RACK, 1))
        );

        FurnitureResponseDto targetFurniture = furnitureList.stream()
                .filter(f -> f.getFurnitureType().equals(furnitureType.name()))
                .findFirst()
                .orElse(null);

        if (targetFurniture == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // `isVisible` 값 토글 (true → false, false → true)
        FurnitureResponseDto updatedFurniture = new FurnitureResponseDto(
                targetFurniture.getFurnitureType(),
                !targetFurniture.getIsVisible(),
                targetFurniture.getLevel(),
                targetFurniture.getMaxCapacity()
        );

        return ResponseEntity.ok(updatedFurniture);
    }

    private RoomResponseDto createMockRoomResponse(Long roomId, String theme) {
        return RoomResponseDto.builder()
                .roomId(roomId)
                .userId(67890L)
                .theme(theme)
                .createdAt(LocalDateTime.now())
                .furnitures(List.of(
                        new FurnitureResponseDto("BOOKSHELF", true, 3, FurnitureCapacity.getCapacity(FurnitureType.BOOKSHELF, 3)),
                        new FurnitureResponseDto("CD_RACK", false, 1, FurnitureCapacity.getCapacity(FurnitureType.CD_RACK, 1))
                ))
                .storageLimits(new StorageLimitsDto(100, 50))
                .userStorage(new UserStorageDto(25L, 10L, 5L, 7L))
                .build();
    }
}
