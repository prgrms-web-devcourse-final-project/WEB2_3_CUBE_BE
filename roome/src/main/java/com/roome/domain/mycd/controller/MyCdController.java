package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.service.MyCdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mycd")
@RequiredArgsConstructor
public class MyCdController {

  private final MyCdService myCdService;

  @PostMapping
  public ResponseEntity<MyCdResponse> addMyCd(
      @RequestParam Long userId,
      @RequestBody @Valid MyCdCreateRequest myCdRequest
  ) {
    return ResponseEntity.ok(myCdService.addCdToMyList(userId, myCdRequest));
  }

  @GetMapping
  public ResponseEntity<MyCdListResponse> getMyCdList(
      @RequestParam Long userId,
      @RequestParam(value = "cursor", required = false) Long cursor,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(myCdService.getMyCdList(userId, cursor, size));
  }

  @GetMapping("/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(
      @RequestParam Long userId,
      @PathVariable Long myCdId
  ) {
    return ResponseEntity.ok(myCdService.getMyCd(userId, myCdId));
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(
      @RequestParam Long userId,
      @RequestParam String myCdIds
  ) {
    myCdService.delete(userId, myCdIds);
    return ResponseEntity.noContent().build();
  }
}
