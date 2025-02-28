package com.roome.domain.cdtemplate.controller;

import com.roome.domain.cdtemplate.dto.CdTemplateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import com.roome.domain.cdtemplate.service.CdTemplateService;
import com.roome.global.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CD 템플릿", description = "CD 템플릿 관련 API")
@RestController
@RequestMapping("/api/my-cd/{myCdId}/template")
@RequiredArgsConstructor
public class CdTemplateController {

  private final CdTemplateService cdTemplateService;

  @Operation(summary = "CD 템플릿 생성", description = "사용자가 특정 CD에 대한 템플릿을 생성합니다.")
  @PostMapping
  public ResponseEntity<CdTemplateResponse> createTemplate(
      @AuthenticatedUser Long userId,
      @Parameter(description = "템플릿을 추가할 CD의 ID", example = "1") @PathVariable Long myCdId,
      @RequestBody CdTemplateRequest request
  ) {
    return ResponseEntity.ok(cdTemplateService.createTemplate(myCdId, userId, request));
  }

  @Operation(summary = "CD 템플릿 조회", description = "특정 CD의 템플릿을 조회합니다.")
  @GetMapping
  public ResponseEntity<CdTemplateResponse> getTemplate(
      @Parameter(description = "조회할 CD의 ID", example = "1") @PathVariable Long myCdId
  ) {
    return ResponseEntity.ok(cdTemplateService.getTemplate(myCdId));
  }

  @Operation(summary = "CD 템플릿 수정", description = "사용자가 특정 CD의 템플릿을 수정합니다.")
  @PatchMapping
  public ResponseEntity<CdTemplateResponse> updateTemplate(
      @AuthenticatedUser Long userId,
      @Parameter(description = "수정할 CD의 ID", example = "1") @PathVariable Long myCdId,
      @RequestBody CdTemplateRequest request
  ) {
    return ResponseEntity.ok(cdTemplateService.updateTemplate(myCdId, userId, request));
  }

  @Operation(summary = "CD 템플릿 삭제", description = "사용자가 특정 CD의 템플릿을 삭제합니다.")
  @DeleteMapping
  public ResponseEntity<Void> deleteTemplate(
      @AuthenticatedUser Long userId,
      @Parameter(description = "삭제할 CD의 ID", example = "1") @PathVariable Long myCdId
  ) {
    cdTemplateService.deleteTemplate(myCdId, userId);
    return ResponseEntity.noContent().build();
  }
}
