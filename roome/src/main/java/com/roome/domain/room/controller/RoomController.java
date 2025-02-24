package com.roome.domain.room.controller;

import com.roome.domain.room.dto.RoomResponseDto;
import com.roome.domain.room.dto.UpdateRoomThemeRequestDto;
import com.roome.domain.room.service.RoomService;
import com.roome.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Map<String, Object>> updateRoomTheme(
            @RequestParam("userId") Long userId,
            @PathVariable Long roomId,
            @RequestBody UpdateRoomThemeRequestDto requestDto
    ) {
        String updatedTheme = roomService.updateRoomTheme(userId, roomId, requestDto.getThemeName());

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", roomId);
        response.put("updatedTheme", updatedTheme);

        return ResponseEntity.ok(response);
    }
}