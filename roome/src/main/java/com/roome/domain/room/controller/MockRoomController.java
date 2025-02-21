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
@Tag(name = "Room", description = "방 조회/테마 업데이트")
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
        RoomResponseDto roomResponseDto = createMockRoomResponse(1L, "vintage");
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
