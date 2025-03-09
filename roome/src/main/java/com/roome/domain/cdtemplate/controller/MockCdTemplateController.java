package com.roome.domain.cdtemplate.controller;

import com.roome.domain.cdtemplate.dto.CdTemplateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Tag(name = "Mock - CD Template", description = "CD 템플릿 작성/조회/수정/삭제")
@RestController
@RequestMapping("/mock/my-cd/{myCdId}/template")
public class MockCdTemplateController {

  private final AtomicLong templateIdCounter = new AtomicLong(1);

  @Operation(summary = "Mock - CD 템플릿 작성", description = "CD 템플릿을 작성합니다.")
  @PostMapping
  public ResponseEntity<CdTemplateResponse> createTemplate(
      @Parameter(description = "CD ID", required = true) @PathVariable Long myCdId,
      @Parameter(description = "사용자 ID", required = true) @RequestParam Long userId,
      @RequestBody CdTemplateRequest request
  ) {
    CdTemplateResponse response = new CdTemplateResponse(
        templateIdCounter.getAndIncrement(),
        myCdId,
        request.getComment1(),
        request.getComment2(),
        request.getComment3(),
        request.getComment4()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - CD 템플릿 조회", description = "CD 템플릿을 조회합니다.")
  @GetMapping
  public ResponseEntity<CdTemplateResponse> getTemplate(
      @Parameter(description = "CD ID", required = true) @PathVariable Long myCdId
  ) {
    CdTemplateResponse response = new CdTemplateResponse(
        5L,
        myCdId,
        "CD를 듣게 된 계기",
        "CD에서 가장 좋았던 부분",
        "CD를 들으며 느낀 감정",
        "자주 듣는 상황"
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - CD 템플릿 수정", description = "CD 템플릿을 수정합니다.")
  @PatchMapping
  public ResponseEntity<CdTemplateResponse> updateTemplate(
      @Parameter(description = "CD ID", required = true) @PathVariable Long myCdId,
      @Parameter(description = "사용자 ID", required = true) @RequestParam Long userId,
      @RequestBody CdTemplateRequest request
  ) {
    CdTemplateResponse response = new CdTemplateResponse(
        5L,
        myCdId,
        request.getComment1(),
        request.getComment2(),
        request.getComment3(),
        request.getComment4()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Mock - CD 템플릿 삭제", description = "CD 템플릿을 삭제합니다.")
  @DeleteMapping
  public ResponseEntity<Void> deleteTemplate(
      @Parameter(description = "CD ID", required = true) @PathVariable Long myCdId,
      @Parameter(description = "사용자 ID", required = true) @RequestParam Long userId
  ) {
    return ResponseEntity.noContent().build();
  }
}
