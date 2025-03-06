package com.roome.domain.room.controller;

import com.roome.domain.furniture.dto.FurnitureRequestDto;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.dto.ToggleFurnitureResponseDto;
import com.roome.domain.furniture.entity.FurnitureCapacity;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.room.dto.*;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/mock/rooms")
@RequiredArgsConstructor
@Tag(name = "Mock - Room API", description = "방 조회/테마 업데이트/가구 활성화 및 비활성화")
public class MockRoomController {

    private List<String> getMockTop3BookGenres() {
        return List.of("Fantasy", "Mystery", "Science Fiction");
    }

    private List<String> getMockTop3CdGenres() {
        return List.of("Rock", "Pop", "Jazz");
    }


    @Operation(summary = "Mock - 방 조회", description = "주어진 방 ID에 해당하는 방 정보 조회")
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDto> getRoom(
            @PathVariable Long roomId
    ) {
        RoomResponseDto roomResponseDto = createMockRoomResponse(roomId, "basic");
        return ResponseEntity.ok(roomResponseDto);
    }

    @Operation(summary = "Mock - 사용자 방 조회", description = "주어진 사용자 ID에 해당하는 방 정보 조회")
    @GetMapping
    public ResponseEntity<RoomResponseDto> getRoomByUserId(
            @AuthenticationPrincipal Long userId
    ){
        RoomResponseDto roomResponseDto = createMockRoomResponse(1L, "forest");
        return ResponseEntity.ok(roomResponseDto);
    }

    @Operation(summary = "Mock - 방 테마 업데이트", description = "사용자가 지정한 테마로 업데이트")
    @PutMapping("/{roomId}")
    public ResponseEntity<UpdateRoomThemeResponseDto> updateRoomTheme(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @RequestBody UpdateRoomThemeRequestDto requestDto
    ) {
        UpdateRoomThemeResponseDto response = new UpdateRoomThemeResponseDto(roomId, requestDto.getThemeName());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mock - 가구 활성화/비활성화", description = "주어진 방에서 특정 가구를 활성화하거나 비활성화함")
    @PutMapping("/{roomId}/furniture")
    public ResponseEntity<ToggleFurnitureResponseDto> toggleFurnitureVisibility(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @RequestBody FurnitureRequestDto requestDto
    ) {
        String furnitureTypeStr = requestDto.getFurnitureType();

        if (furnitureTypeStr == null || furnitureTypeStr.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FURNITURE_TYPE);
        }

        FurnitureType furnitureType;
        try {
            furnitureType = FurnitureType.valueOf(furnitureTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_FURNITURE_TYPE);
        }

        List<String> topGenres = switch (furnitureType) {
            case BOOKSHELF -> getMockTop3BookGenres();
            case CD_RACK -> getMockTop3CdGenres();
        };

        // 가구 리스트에서 해당 타입 찾기
        List<FurnitureResponseDto> furnitureList = List.of(
                new FurnitureResponseDto("BOOKSHELF", true, 3, FurnitureCapacity.getCapacity(FurnitureType.BOOKSHELF, 3), getMockTop3BookGenres()),
                new FurnitureResponseDto("CD_RACK", false, 1, FurnitureCapacity.getCapacity(FurnitureType.CD_RACK, 1), getMockTop3CdGenres())
        );

        FurnitureResponseDto targetFurniture = furnitureList.stream()
                .filter(f -> f.getFurnitureType().equals(furnitureType.name()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.FURNITURE_NOT_FOUND));


        // `isVisible` 값 토글 (true → false, false → true)
        FurnitureResponseDto updatedFurniture = new FurnitureResponseDto(
                targetFurniture.getFurnitureType(),
                !targetFurniture.getIsVisible(),
                targetFurniture.getLevel(),
                targetFurniture.getMaxCapacity(),
                topGenres
        );

        ToggleFurnitureResponseDto responseDto = new ToggleFurnitureResponseDto(roomId, updatedFurniture);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Mock - 사용자가 잠금 해제한 테마 목록 조회", description = "해당 사용자가 잠금 해제한 방 테마 목록을 반환한다.")
    @GetMapping("/{userId}/unlocked-themes")
    public ResponseEntity<List<String>> getUnlockedThemes(@AuthenticationPrincipal Long userId) {
        List<String> unlockedThemes = List.of("basic", "forest", "marine");

        return ResponseEntity.ok(unlockedThemes);
    }

    private RoomResponseDto createMockRoomResponse(Long roomId, String theme) {
        return RoomResponseDto.builder()
                .roomId(roomId)
                .userId(67890L)
                .theme(theme)
                .createdAt(LocalDateTime.now())
                .furnitures(List.of(
                        new FurnitureResponseDto("BOOKSHELF", true, 3, FurnitureCapacity.getCapacity(FurnitureType.BOOKSHELF, 3), getMockTop3BookGenres()),
                        new FurnitureResponseDto("CD_RACK", false, 1, FurnitureCapacity.getCapacity(FurnitureType.CD_RACK, 1), getMockTop3CdGenres())
                ))
                .storageLimits(new StorageLimitsDto(100, 50))
                .userStorage(new UserStorageDto(25L, 10L, 5L, 7L))
                .build();
    }
}
