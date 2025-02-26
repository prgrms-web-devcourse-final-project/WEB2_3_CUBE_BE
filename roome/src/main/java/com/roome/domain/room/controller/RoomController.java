package com.roome.domain.room.controller;

import com.roome.domain.furniture.dto.FurnitureRequestDto;
import com.roome.domain.furniture.dto.FurnitureResponseDto;
import com.roome.domain.furniture.dto.ToggleFurnitureResponseDto;
import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.UpdateRoomThemeRequestDto;
import com.roome.domain.room.dto.UpdateRoomThemeResponseDto;
import com.roome.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDto> getRoom(
            @PathVariable Long roomId
    ) {
        RoomResponseDto roomResponseDto = roomService.getRoomById(roomId);
        return ResponseEntity.ok(roomResponseDto);
    }

    @GetMapping
    public ResponseEntity<RoomResponseDto> getRoomByUserId(
            @RequestParam("userId") Long userId
    ){
        RoomResponseDto roomResponseDto = roomService.getRoomByUserId(userId);
        return ResponseEntity.ok(roomResponseDto);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<UpdateRoomThemeResponseDto> updateRoomTheme(
            @RequestParam("userId") Long userId,
            @PathVariable Long roomId,
            @RequestBody UpdateRoomThemeRequestDto requestDto
    ) {
        String updatedTheme = roomService.updateRoomTheme(userId, roomId, requestDto.getThemeName());

        UpdateRoomThemeResponseDto responseDto = new UpdateRoomThemeResponseDto(roomId, updatedTheme);

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{roomId}/furniture")
    public ResponseEntity<ToggleFurnitureResponseDto> toggleFurnitureVisibility(
            @RequestParam("userId") Long userId,
            @PathVariable Long roomId,
            @RequestBody FurnitureRequestDto furnitureRequestDto
            ) {
        String furnitureType = furnitureRequestDto.getFurnitureType();
        FurnitureResponseDto updatedFurniture = roomService.toggleFurnitureVisibility(userId, roomId, furnitureType);

        ToggleFurnitureResponseDto responseDto = new ToggleFurnitureResponseDto(roomId, updatedFurniture);

        return ResponseEntity.ok(responseDto);
    }
}