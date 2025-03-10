package com.roome.domain.room.controller;

import com.roome.domain.furniture.dto.FurnitureRequestDto;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.dto.ToggleFurnitureResponseDto;
import com.roome.domain.room.dto.PurchaseRoomThemeResponseDto;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.UpdateRoomThemeRequestDto;
import com.roome.domain.room.dto.UpdateRoomThemeResponseDto;
import com.roome.domain.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "방 관리 API", description = "방 조회, 테마 업데이트, 가구 활성화 및 비활성화 기능 제공")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;

  @Operation(summary = "방 조회", description = "방 ID를 통해 방 정보를 조회한다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "방 조회 성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 방 (ROOM_NOT_FOUND)")
  })
  @GetMapping("/{roomId}")
  public ResponseEntity<RoomResponseDto> getRoom(
      @Parameter(description = "조회할 방의 ID")
      @PathVariable Long roomId
  ) {
    RoomResponseDto roomResponseDto = roomService.getRoomById(roomId);
    return ResponseEntity.ok(roomResponseDto);
  }

  @Operation(summary = "사용자별 방 조회", description = "사용자 ID를 통해 해당 사용자의 방 정보를 조회한다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "방 조회 성공"),
      @ApiResponse(responseCode = "404", description = "사용자의 방이 존재하지 않음 (ROOM_NOT_FOUND)")
  })
  @GetMapping
  public ResponseEntity<RoomResponseDto> getRoomByUserId(
      @Parameter(description = "조회할 사용자 ID")
      @RequestParam("userId") Long userId
  ) {
    RoomResponseDto roomResponseDto = roomService.getRoomByUserId(userId);
    return ResponseEntity.ok(roomResponseDto);
  }

  @Operation(summary = "방 테마 변경", description = "사용자가 소유한 방의 테마를 업데이트한다. (구매한 테마만 변경 가능)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "테마 변경 성공"),
      @ApiResponse(responseCode = "400", description = "사용자가 테마를 보유하고 있지 않음 (THEME_NOT_OWNED)"),
      @ApiResponse(responseCode = "403", description = "방 소유자가 아님 (ROOM_ACCESS_DENIED)")
  })
  @PutMapping("/{roomId}")
  public ResponseEntity<UpdateRoomThemeResponseDto> updateRoomTheme(
      @AuthenticationPrincipal Long userId,
      @Parameter(description = "변경할 방의 ID")
      @PathVariable Long roomId,
      @RequestBody UpdateRoomThemeRequestDto requestDto
  ) {
    String updatedTheme = roomService.updateRoomTheme(userId, roomId, requestDto.getThemeName());

    UpdateRoomThemeResponseDto responseDto = new UpdateRoomThemeResponseDto(roomId, updatedTheme);
    return ResponseEntity.ok(responseDto);
  }

  @Operation(summary = "방 테마 구매", description = "포인트를 사용하여 방 테마를 구매하고 잠금 해제한다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "테마 구매 성공"),
      @ApiResponse(responseCode = "400", description = "포인트 부족 (INSUFFICIENT_POINTS)")
  })
  @PostMapping("/{roomId}/purchase-theme")
  public ResponseEntity<PurchaseRoomThemeResponseDto> purchaseRoomTheme(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long roomId,
      @RequestBody UpdateRoomThemeRequestDto requestDto
  ) {
    int remainingPoints = roomService.purchaseRoomTheme(userId, roomId, requestDto.getThemeName());

    PurchaseRoomThemeResponseDto responseDto = new PurchaseRoomThemeResponseDto(
        roomId,
        requestDto.getThemeName(),
        remainingPoints
    );

    return ResponseEntity.ok(responseDto);
  }


  @Operation(summary = "가구 표시 여부 토글", description = "사용자가 특정 방의 가구를 표시하거나 숨길 수 있다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "가구 표시 상태 변경 성공"),
      @ApiResponse(responseCode = "403", description = "방 소유자가 아님 (ROOM_ACCESS_DENIED)")
  })
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


  @Operation(summary = "다른 사용자 방 방문", description = "방문자가 다른 사용자의 방을 방문하고 랭킹 점수를 부여한다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "방 방문 성공"),
      @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음 (ROOM_NOT_FOUND)")
  })
  @PostMapping("/visit/{roomId}")
  public ResponseEntity<RoomResponseDto> visitRoom(
      @Parameter(description = "방문자의 ID")
      @RequestParam("visitorId") Long visitorId,
      @Parameter(description = "방문할 방의 ID")
      @PathVariable Long roomId
  ) {
    RoomResponseDto roomResponseDto = roomService.visitRoomByRoomId(visitorId, roomId);
    return ResponseEntity.ok(roomResponseDto);
  }

  @Operation(summary = "사용자별 방 방문", description = "방문자가 다른 사용자의 방을 방문하고 랭킹 점수 부여 (사용자 ID 기준)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "방 방문 성공"),
      @ApiResponse(responseCode = "404", description = "사용자의 방을 찾을 수 없음 (ROOM_NOT_FOUND)")
  })
  @PostMapping("/visit")
  public ResponseEntity<RoomResponseDto> visitUserRoom(
      @Parameter(description = "방문자의 ID")
      @RequestParam("visitorId") Long visitorId,
      @Parameter(description = "방문할 방의 소유자 ID")
      @RequestParam("hostId") Long hostId
  ) {
    RoomResponseDto roomResponseDto = roomService.visitRoomByHostId(visitorId, hostId);
    return ResponseEntity.ok(roomResponseDto);
  }
}