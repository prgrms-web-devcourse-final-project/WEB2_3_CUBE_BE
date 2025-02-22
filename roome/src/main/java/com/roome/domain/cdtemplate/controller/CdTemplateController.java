package com.roome.domain.cdtemplate.controller;

import com.roome.domain.cdtemplate.dto.CdTemplateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import com.roome.domain.cdtemplate.service.CdTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mycd/{myCdId}/template")
@RequiredArgsConstructor
public class CdTemplateController {

  private final CdTemplateService cdTemplateService;

  @PostMapping
  public ResponseEntity<CdTemplateResponse> createTemplate(
      @PathVariable Long myCdId,
      @RequestParam Long userId,
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
      @PathVariable Long myCdId,
      @RequestParam Long userId,
      @RequestBody CdTemplateRequest request
  ) {
    return ResponseEntity.ok(cdTemplateService.updateTemplate(myCdId, userId, request));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteTemplate(@PathVariable Long myCdId, @RequestParam Long userId) {
    cdTemplateService.deleteTemplate(myCdId, userId);
    return ResponseEntity.noContent().build();
  }
}
