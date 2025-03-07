package com.roome.domain.houseMate.controller;

import com.roome.domain.houseMate.dto.HousemateInfo;
import com.roome.domain.houseMate.dto.HousemateListResponse;
import com.roome.domain.user.entity.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "하우스메이트 Mock API", description = "하우스메이트 관리를 위한 Mock API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mock/mates")
public class MockHouseMateController {

    //mock 데이터 생성
    private final List<HousemateInfo> mockHousemates = new ArrayList<>();

    {
        for (long i = 1; i <= 50; i++) {
            mockHousemates.add(
                    HousemateInfo.builder()
                            .userId(i)
                            .nickname("사용자 " + i)
                            .profileImage("https://github.com/user-attachments/assets/mock-image-" + i)
                            .bio("사용자 " + i + "의 상태메시지입니다.")
                            .status(i % 2 == 0 ? Status.ONLINE : Status.OFFLINE)
                            .build()
            );
        }
    }

    @Operation(summary = "나를 추가한 메이트 목록 조회",
            description = "현재 사용자를 하우스메이트로 추가한 사용자들의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메이트 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 커서 형식 또는 유효하지 않은 limit 값")
    })
    @GetMapping("/followers")
    public ResponseEntity<HousemateListResponse> getFollowers(
            @Parameter(description = "페이지네이션 커서 (마지막으로 받은 userId)", example = "10")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "한 페이지당 조회할 메이트 수", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "닉네임으로 검색", example = "김철수")
            @RequestParam(required = false) String nickname) {

        List<HousemateInfo> filteredList = mockHousemates.stream()
                .filter(mate -> cursor == null || mate.getUserId() > cursor)
                .filter(mate -> nickname == null ||
                        mate.getNickname().toLowerCase().contains(nickname.toLowerCase()))
                .limit(limit + 1L)
                .collect(Collectors.toList());

        boolean hasNext = filteredList.size() > limit;
        List<HousemateInfo> resultList = hasNext
                ? filteredList.subList(0, limit)
                : filteredList;

        Long nextCursor = hasNext && !resultList.isEmpty()
                ? resultList.get(resultList.size() - 1).getUserId()
                : null;

        HousemateListResponse response = HousemateListResponse.builder()
                .housemates(resultList)
                .nextCursor(nextCursor != null ? nextCursor : null)
                .hasNext(hasNext)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 추가한 메이트 목록 조회",
            description = "현재 사용자가 하우스메이트로 추가한 사용자들의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메이트 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 커서 형식 또는 유효하지 않은 limit 값")
    })
    @GetMapping("/following")
    public ResponseEntity<HousemateListResponse> getFollowing(
            @Parameter(description = "페이지네이션 커서 (마지막으로 받은 userId)", example = "10")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "한 페이지당 조회할 메이트 수", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "닉네임으로 검색", example = "김철수")
            @RequestParam(required = false) String nickname) {

        List<HousemateInfo> filteredList = mockHousemates.stream()
                .filter(mate -> cursor == null || mate.getUserId() > cursor)
                .filter(mate -> nickname == null ||
                        mate.getNickname().toLowerCase().contains(nickname.toLowerCase()))
                .limit(limit + 1)
                .collect(Collectors.toList());

        boolean hasNext = filteredList.size() > limit;
        List<HousemateInfo> resultList = hasNext
                ? filteredList.subList(0, limit)
                : filteredList;

        Long nextCursor = hasNext && !resultList.isEmpty()
                ? resultList.get(resultList.size() - 1).getUserId()
                : null;

        HousemateListResponse response = HousemateListResponse.builder()
                .housemates(resultList)
                .nextCursor(nextCursor != null ? nextCursor : null)
                .hasNext(hasNext)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "하우스메이트 추가",
            description = "특정 사용자를 하우스메이트로 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "하우스메이트 추가 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @PostMapping("/follow/{targetId}")
    public ResponseEntity<Void> addHousemate(
            @Parameter(description = "추가할 사용자의 ID", example = "1")
            @PathVariable Long targetId) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "하우스메이트 삭제",
            description = "특정 사용자를 하우스메이트에서 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "하우스메이트 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @DeleteMapping("/follow/{targetId}")
    public ResponseEntity<Void> removeHousemate(
            @Parameter(description = "삭제할 사용자의 ID", example = "1")
            @PathVariable Long targetId) {
        return ResponseEntity.noContent().build();
    }
}