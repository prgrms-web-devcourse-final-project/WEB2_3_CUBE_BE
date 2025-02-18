package com.roome.domain.houseMate.controller;

import com.roome.domain.houseMate.dto.HousemateListResponse;
import com.roome.domain.houseMate.service.HousemateService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.file.attribute.UserPrincipal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mates")
public class HousemateController {

    private final HousemateService housemateService;

    @GetMapping("/followers")
    public ResponseEntity<HousemateListResponse> getFollowers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Max(100) @Min(1) int limit,
            @RequestParam(required = false) String nickname) {

        Long UserId = housemateService.findUserIdByUserId(principal.getName());
        HousemateListResponse response = housemateService.getFollowerList(
                UserId, cursor, limit, nickname);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/following")
    public ResponseEntity<HousemateListResponse> getFollowing(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Max(100) @Min(1) int limit,
            @RequestParam(required = false) String nickname) {

        Long UserId = housemateService.findUserIdByUserId(principal.getName());
        HousemateListResponse response = housemateService.getFollowingList(
                UserId, cursor, limit, nickname);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/follow/{targetId}")
    public ResponseEntity<Void> addHousemate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long targetId) {

        Long UserId = housemateService.findUserIdByUserId(principal.getName());
        housemateService.addHousemate(UserId, targetId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/follow/{targetId}")
    public ResponseEntity<Void> removeHousemate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long targetId) {

        Long UserId = housemateService.findUserIdByUserId(principal.getName());
        housemateService.removeHousemate(UserId, targetId);

        return ResponseEntity.noContent().build();
    }
}