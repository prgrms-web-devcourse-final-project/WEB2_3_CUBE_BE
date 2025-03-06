package com.roome.domain.room.controller;

import com.roome.domain.furniture.dto.FurnitureRequestDto;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.dto.ToggleFurnitureResponseDto;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.UpdateRoomThemeRequestDto;
import com.roome.domain.room.dto.UpdateRoomThemeResponseDto;
import com.roome.domain.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Room API", description = "방 조회/테마 업데이트/가구 활성화 및 비활성화")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;

  @Operation(summary = "방 조회", description = "주어진 방 ID를 통해 방 정보 조회")
  @GetMapping("/{roomId}")
  public ResponseEntity<RoomResponseDto> getRoom(
      @PathVariable Long roomId
  ) {
    RoomResponseDto roomResponseDto = roomService.getRoomById(roomId);
    return ResponseEntity.ok(roomResponseDto);
  }

  @Operation(summary = "사용자별 방 조회", description = "주어진 사용자 ID를 통해 해당 사용자의 방 정보 조회")
  @GetMapping
  public ResponseEntity<RoomResponseDto> getRoomByUserId(
      @RequestParam("userId") Long userId
  ) {
    RoomResponseDto roomResponseDto = roomService.getRoomByUserId(userId);
    return ResponseEntity.ok(roomResponseDto);
  }

  @Operation(summary = "방 테마 변경", description = "주어진 방 ID와 사용자 ID를 통해 방 테마 업데이트")
  @PutMapping("/{roomId}")
  public ResponseEntity<UpdateRoomThemeResponseDto> updateRoomTheme(
          @AuthenticationPrincipal Long userId,
      @PathVariable Long roomId,
      @RequestBody UpdateRoomThemeRequestDto requestDto
  ) {
    String updatedTheme = roomService.updateRoomTheme(userId, roomId, requestDto.getThemeName());

    UpdateRoomThemeResponseDto responseDto = new UpdateRoomThemeResponseDto(roomId, updatedTheme);

    return ResponseEntity.ok(responseDto);
  }

  @Operation(summary = "가구 표시 여부 토글", description = "주어진 방 ID와 가구 타입을 통해 가구의 표시 여부 토글")
  @PutMapping("/{roomId}/furniture")
  public ResponseEntity<ToggleFurnitureResponseDto> toggleFurnitureVisibility(
          @AuthenticationPrincipal Long userId,
      @PathVariable Long roomId,
      @RequestBody FurnitureRequestDto furnitureRequestDto
  ) {
    String furnitureType = furnitureRequestDto.getFurnitureType();
    FurnitureResponseDto updatedFurniture = roomService.toggleFurnitureVisibility(userId, roomId,
        furnitureType);

    ToggleFurnitureResponseDto responseDto = new ToggleFurnitureResponseDto(roomId,
        updatedFurniture);

    return ResponseEntity.ok(responseDto);
  }

  @Operation(summary = "사용자가 잠금 해제한 테마 목록 조회", description = "해당 사용자가 잠금 해제한 방 테마 목록을 반환한다.")
  @GetMapping("/{userId}/unlocked-themes")
  public ResponseEntity<List<String>> getUnlockedThemes(
          @AuthenticationPrincipal Long userId
  ) {
    List<String> unlockedThemes = roomService.getUnlockedThemes(userId);
    return ResponseEntity.ok(unlockedThemes);
  }


  @Operation(summary = "다른 사용자 방 방문", description = "방문자가 다른 사용자의 방을 방문하고 랭킹 점수 부여")
  @PostMapping("/visit/{roomId}")
  public ResponseEntity<RoomResponseDto> visitRoom(
      @RequestParam("visitorId") Long visitorId,
      @PathVariable Long roomId
  ) {
    RoomResponseDto roomResponseDto = roomService.visitRoomByRoomId(visitorId, roomId);
    return ResponseEntity.ok(roomResponseDto);
  }

  @Operation(summary = "사용자별 방 방문", description = "방문자가 다른 사용자의 방을 방문하고 랭킹 점수 부여 (사용자 ID 기준)")
  @PostMapping("/visit")
  public ResponseEntity<RoomResponseDto> visitUserRoom(
      @RequestParam("visitorId") Long visitorId,
      @RequestParam("hostId") Long hostId
  ) {
    RoomResponseDto roomResponseDto = roomService.visitRoomByHostId(visitorId, hostId);
    return ResponseEntity.ok(roomResponseDto);
  }
}