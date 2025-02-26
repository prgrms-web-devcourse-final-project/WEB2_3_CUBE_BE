package com.roome.domain.furniture.controller;

import com.roome.domain.furniture.service.FurnitureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FurnitureController {

    private final FurnitureService furnitureService;

    @PostMapping("/api/rooms/{roomId}/furniture/bookshelf")
    public ResponseEntity<Void> upgradeBookshelf(
            @PathVariable("roomId") Long roomId,
            @RequestParam("level") int selectedLevel
    ) {
        Long loginUserId = 1L;
        furnitureService.upgradeBookshelf(loginUserId, roomId, selectedLevel);
        return ResponseEntity.ok().build();
    }
}
