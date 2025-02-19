package com.roome.domain.houseMate.controller;

import com.roome.domain.houseMate.dto.HousemateInfo;
import com.roome.domain.houseMate.dto.HousemateListResponse;
import com.roome.domain.user.entity.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mock/mates")
public class MockHouseMateController {

    private final List<HousemateInfo> mockHousemates = Arrays.asList(
            HousemateInfo.builder()
                         .userId(1L)
                         .nickname("John Doe")
                         .profileImage("https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
                         .bio("Mock bio 1")
                         .status(Status.ONLINE)
                         .build(),
            HousemateInfo.builder()
                         .userId(2L)
                         .nickname("Jane Smith")
                         .profileImage("https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
                         .bio("Mock bio 2")
                         .status(Status.OFFLINE)
                         .build(),
            HousemateInfo.builder()
                         .userId(3L)
                         .nickname("Mike Johnson")
                         .profileImage("https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
                         .bio("Mock bio 3")
                         .status(Status.ONLINE)
                         .build()
                                                                    );

    @GetMapping("/followers")
    public ResponseEntity<HousemateListResponse> getFollowers(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String nickname) {

        HousemateListResponse response = HousemateListResponse.builder()
                                                              .housemates(mockHousemates)
                                                              .nextCursor("mock_next_cursor")
                                                              .hasNext(false)
                                                              .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/following")
    public ResponseEntity<HousemateListResponse> getFollowing(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String nickname) {

        HousemateListResponse response = HousemateListResponse.builder()
                                                              .housemates(mockHousemates)
                                                              .nextCursor("mock_next_cursor")
                                                              .hasNext(false)
                                                              .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/follow/{targetId}")
    public ResponseEntity<Void> addHousemate(@PathVariable Long targetId) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/follow/{targetId}")
    public ResponseEntity<Void> removeHousemate(@PathVariable Long targetId) {
        return ResponseEntity.noContent().build();
    }
}