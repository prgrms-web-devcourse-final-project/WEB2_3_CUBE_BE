package com.roome.domain.room.controller;

import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.UpdateRoomThemeRequestDto;
import com.roome.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @AuthenticationPrincipal Long userId
    ){
        RoomResponseDto roomResponseDto = roomService.getRoomByUserId(userId);
        return ResponseEntity.ok(roomResponseDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{roomId}")
    public RoomResponseDto updateRoomTheme(
            @PathVariable Long roomId,
            @RequestBody UpdateRoomThemeRequestDto requestDto
            ) {
        return roomService.updateRoomTheme(roomId, requestDto.getThemeName());
    }
}