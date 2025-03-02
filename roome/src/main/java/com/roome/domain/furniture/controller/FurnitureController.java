package com.roome.domain.furniture.controller;

import com.roome.domain.furniture.service.FurnitureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal Long loginUserId,
            @PathVariable("roomId") Long roomId,
            @RequestParam("level") int selectedLevel
    ) {
        furnitureService.upgradeBookshelf(loginUserId, roomId, selectedLevel);
        return ResponseEntity.ok().build();
    }
}
