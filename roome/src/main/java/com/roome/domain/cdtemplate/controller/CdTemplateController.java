package com.roome.domain.cdtemplate.controller;

import com.roome.domain.cdtemplate.dto.CdTemplateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import com.roome.domain.cdtemplate.service.CdTemplateService;
import com.roome.global.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-cd/{myCdId}/template")
@RequiredArgsConstructor
public class CdTemplateController {

  private final CdTemplateService cdTemplateService;

  @PostMapping
  public ResponseEntity<CdTemplateResponse> createTemplate(
      @AuthenticatedUser Long userId,
      @PathVariable Long myCdId,
      @RequestBody CdTemplateRequest request
  ) {
    return ResponseEntity.ok(cdTemplateService.createTemplate(myCdId, userId, request));
  }

  @GetMapping
  public ResponseEntity<CdTemplateResponse> getTemplate(@PathVariable Long myCdId) {
    return ResponseEntity.ok(cdTemplateService.getTemplate(myCdId));
  }

  @PatchMapping
  public ResponseEntity<CdTemplateResponse> updateTemplate(
      @AuthenticatedUser Long userId,
      @PathVariable Long myCdId,
      @RequestBody CdTemplateRequest request
  ) {
    return ResponseEntity.ok(cdTemplateService.updateTemplate(myCdId, userId, request));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteTemplate(
      @AuthenticatedUser Long userId,
      @PathVariable Long myCdId
  ) {
    cdTemplateService.deleteTemplate(myCdId, userId);
    return ResponseEntity.noContent().build();
  }
}
