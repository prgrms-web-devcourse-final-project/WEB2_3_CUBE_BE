package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.service.MyCdService;
import com.roome.global.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-cd")
@RequiredArgsConstructor
public class MyCdController {

  private final MyCdService myCdService;

  @PostMapping
  public ResponseEntity<MyCdResponse> addMyCd(
      @AuthenticatedUser Long userId,
      @RequestBody @Valid MyCdCreateRequest myCdRequest
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(myCdService.addCdToMyList(userId, myCdRequest));
  }

  @GetMapping
  public ResponseEntity<MyCdListResponse> getMyCdList(
      @AuthenticatedUser Long userId,
      @RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor,  // ✅ 기본값 0L 설정
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(myCdService.getMyCdList(userId, cursor, size));
  }

  @GetMapping("/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(
      @AuthenticatedUser Long userId,
      @PathVariable Long myCdId
  ) {
    return ResponseEntity.ok(myCdService.getMyCd(userId, myCdId));
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(
      @AuthenticatedUser Long userId,
      @RequestParam List<Long> myCdIds
  ) {
    myCdService.delete(userId, myCdIds);
    return ResponseEntity.noContent().build();
  }
}
