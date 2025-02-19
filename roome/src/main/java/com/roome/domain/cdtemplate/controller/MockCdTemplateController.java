package com.roome.domain.cdtemplate.controller;

import com.roome.domain.cdtemplate.dto.CdTemplateCreateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import com.roome.domain.cdtemplate.dto.CdTemplateUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/mock/api/mycd/{myCdId}/template")
public class MockCdTemplateController {

  private final AtomicLong templateIdCounter = new AtomicLong(1);

  @Operation(summary = "Mock - CD 템플릿 작성", description = "CD 템플릿을 작성합니다.")
  @PostMapping
  public ResponseEntity<CdTemplateResponse> createTemplate(
      @PathVariable Long myCdId,
      @RequestBody CdTemplateCreateRequest request
  ) {
    CdTemplateResponse response = new CdTemplateResponse(
        templateIdCounter.getAndIncrement(),
        myCdId,
        request.getReason(),
        request.getBestPart(),
        request.getEmotion(),
        request.getFrequentSituation(),
        LocalDateTime.now()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - CD 템플릿 조회", description = "CD 템플릿을 조회합니다.")
  @GetMapping
  public ResponseEntity<CdTemplateResponse> getTemplate(@PathVariable Long myCdId) {
    CdTemplateResponse response = new CdTemplateResponse(
        5L,
        myCdId,
        "CD를 듣게 된 계기",
        "CD에서 가장 좋았던 부분",
        "CD를 들으며 느낀 감정",
        "자주 듣는 상황",
        LocalDateTime.now()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - CD 템플릿 수정", description = "CD 템플릿을 수정합니다.")
  @PatchMapping
  public ResponseEntity<CdTemplateResponse> updateTemplate(
      @PathVariable Long myCdId,
      @RequestBody CdTemplateUpdateRequest request
  ) {
    CdTemplateResponse response = new CdTemplateResponse(
        5L,
        myCdId,
        request.getReason(),
        request.getBestPart(),
        request.getEmotion(),
        request.getFrequentSituation(),
        LocalDateTime.now()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - CD 템플릿 삭제", description = "CD 템플릿을 삭제합니다.")
  @DeleteMapping
  public ResponseEntity<Void> deleteTemplate(@PathVariable Long myCdId) {
    return ResponseEntity.noContent().build();
  }
}
