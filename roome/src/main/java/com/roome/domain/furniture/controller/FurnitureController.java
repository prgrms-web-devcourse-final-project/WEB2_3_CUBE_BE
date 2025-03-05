package com.roome.domain.furniture.controller;

import com.roome.domain.furniture.service.FurnitureService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가구 API", description = "가구 레벨 업그레이드")
@RestController
@RequiredArgsConstructor
public class FurnitureController {

  private final FurnitureService furnitureService;

  @Operation(summary = "책장 레벨 업그레이드", description = "책장 레벨을 업그레이드 할 수 있다.")
  @PostMapping("/api/rooms/{roomId}/furniture/bookshelf/upgrade")
  public ResponseEntity<Void> upgradeBookshelf(
      @AuthenticationPrincipal Long loginUserId,
      @PathVariable("roomId") Long roomId
  ) {
    furnitureService.upgradeBookshelf(loginUserId, roomId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "CD 랙 레벨 업그레이드", description = "CD 랙 레벨을 업그레이드 할 수 있다.")
  @PatchMapping("/api/rooms/{roomId}/furniture/cd-rack/upgrade")
  public ResponseEntity<Void> upgradeCdRack(
      @AuthenticatedUser Long userId,
      @PathVariable("roomId") Long roomId
  ) {
    furnitureService.upgradeCdRack(userId, roomId);
    return ResponseEntity.noContent().build();
  }

}
