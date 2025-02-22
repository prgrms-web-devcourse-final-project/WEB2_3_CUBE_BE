package com.roome.domain.cdtemplate.service;

import com.roome.domain.cdtemplate.dto.CdTemplateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import com.roome.domain.cdtemplate.entity.CdTemplate;
import com.roome.domain.cdtemplate.repository.CdTemplateRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CdTemplateService {

  private final CdTemplateRepository cdTemplateRepository;
  private final MyCdRepository myCdRepository;

  // CD 템플릿 작성
  @Transactional
  public CdTemplateResponse createTemplate(Long myCdId, Long userId, CdTemplateRequest request) {
    MyCd myCd = myCdRepository.findById(myCdId)
        .orElseThrow(() -> new IllegalArgumentException("CD 템플릿을 찾을 수 없습니다."));

    // 본인 CD인지 검증
    if (!myCd.getUser().getId().equals(userId)) {
      throw new IllegalStateException("해당 작업을 수행할 권한이 없습니다.");
    }

    CdTemplate cdTemplate = CdTemplate.builder()
        .myCd(myCd)
        .comment1(request.getComment1())
        .comment2(request.getComment2())
        .comment3(request.getComment3())
        .comment4(request.getComment4())
        .build();

    cdTemplateRepository.save(cdTemplate);
    return CdTemplateResponse.from(cdTemplate);
  }

  // CD 템플릿 조회
  public CdTemplateResponse getTemplate(Long myCdId) {
    CdTemplate cdTemplate = cdTemplateRepository.findByMyCdId(myCdId)
        .orElseThrow(() -> new IllegalArgumentException("CD 템플릿을 찾을 수 없습니다."));
    return CdTemplateResponse.from(cdTemplate);
  }

  // 🔥 CD 템플릿 수정
  @Transactional
  public CdTemplateResponse updateTemplate(Long myCdId, Long userId, CdTemplateRequest request) {
    CdTemplate cdTemplate = cdTemplateRepository.findByMyCdId(myCdId)
        .orElseThrow(() -> new IllegalArgumentException("CD 템플릿을 찾을 수 없습니다."));

    // 방 주인(본인)만 수정 가능
    if (!cdTemplate.getMyCd().getUser().getId().equals(userId)) {
      throw new IllegalStateException("해당 작업을 수행할 권한이 없습니다.");
    }

    cdTemplate.update(request.getComment1(), request.getComment2(), request.getComment3(), request.getComment4());
    return CdTemplateResponse.from(cdTemplate);
  }

  // 🔥 CD 템플릿 삭제
  @Transactional
  public void deleteTemplate(Long myCdId, Long userId) {
    CdTemplate cdTemplate = cdTemplateRepository.findByMyCdId(myCdId)
        .orElseThrow(() -> new IllegalArgumentException("CD 템플릿을 찾을 수 없습니다."));

    // 방 주인(본인)만 삭제 가능
    if (!cdTemplate.getMyCd().getUser().getId().equals(userId)) {
      throw new IllegalStateException("해당 작업을 수행할 권한이 없습니다.");
    }

    cdTemplateRepository.delete(cdTemplate);
  }
}
