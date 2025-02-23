package com.roome.domain.cdtemplate.service;

import com.roome.domain.cdtemplate.dto.CdTemplateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import com.roome.domain.cdtemplate.entity.CdTemplate;
import com.roome.domain.cdtemplate.exception.CdTemplateNotFoundException;
import com.roome.domain.cdtemplate.exception.UnauthorizedCdTemplateAccessException;
import com.roome.domain.cdtemplate.repository.CdTemplateRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CdTemplateServiceTest {

  @Mock
  private CdTemplateRepository cdTemplateRepository;

  @Mock
  private MyCdRepository myCdRepository;

  @InjectMocks
  private CdTemplateService cdTemplateService;

  private MyCd myCd;
  private User user;
  private CdTemplate cdTemplate;
  private CdTemplateRequest request;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    user = User.builder()
        .id(1L)
        .nickname("현구")
        .build();

    myCd = MyCd.builder()
        .id(1L)
        .user(user)
        .build();

    cdTemplate = CdTemplate.builder()
        .myCd(myCd)
        .comment1("CD를 듣게 된 계기")
        .comment2("CD에서 가장 좋았던 부분")
        .comment3("CD를 들으며 느낀 감정")
        .comment4("자주 듣는 상황")
        .build();

    request = new CdTemplateRequest(
        "수정된 계기", "수정된 베스트파트", "수정된 감정", "수정된 상황"
    );
  }

  @Test
  @DisplayName("CD 템플릿 생성 성공")
  void createTemplate_Success() {
    // given
    BDDMockito.given(myCdRepository.findById(1L)).willReturn(Optional.of(myCd));
    BDDMockito.given(cdTemplateRepository.save(any(CdTemplate.class))).willReturn(cdTemplate);

    // when
    CdTemplateResponse response = cdTemplateService.createTemplate(1L, 1L, request);

    // then
    assertThat(response.getComment1()).isEqualTo("수정된 계기");
    assertThat(response.getComment2()).isEqualTo("수정된 베스트파트");
  }

  @Test
  @DisplayName("CD 템플릿 생성 실패 - 권한 없음")
  void createTemplate_Unauthorized() {
    // given
    User otherUser = User.builder().id(2L).nickname("다른 사용자").build();
    MyCd otherMyCd = MyCd.builder().id(2L).user(otherUser).build();

    BDDMockito.given(myCdRepository.findById(2L)).willReturn(Optional.of(otherMyCd));

    // when & then
    assertThatThrownBy(() -> cdTemplateService.createTemplate(2L, 1L, request))
        .isInstanceOf(UnauthorizedCdTemplateAccessException.class);
  }

  @Test
  @DisplayName("CD 템플릿 조회 성공")
  void getTemplate_Success() {
    // given
    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.of(cdTemplate));

    // when
    CdTemplateResponse response = cdTemplateService.getTemplate(1L);

    // then
    assertThat(response.getComment1()).isEqualTo(cdTemplate.getComment1());
  }

  @Test
  @DisplayName("CD 템플릿 조회 실패 - 존재하지 않음")
  void getTemplate_NotFound() {
    // given
    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> cdTemplateService.getTemplate(1L))
        .isInstanceOf(CdTemplateNotFoundException.class);
  }

  @Test
  @DisplayName("CD 템플릿 수정 성공")
  void updateTemplate_Success() {
    // given
    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.of(cdTemplate));

    // when
    CdTemplateResponse response = cdTemplateService.updateTemplate(1L, 1L, request);

    // then
    assertThat(response.getComment1()).isEqualTo("수정된 계기");
    assertThat(response.getComment2()).isEqualTo("수정된 베스트파트");
  }

  @Test
  @DisplayName("CD 템플릿 수정 실패 - 존재하지 않음")
  void updateTemplate_NotFound() {
    // given
    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> cdTemplateService.updateTemplate(1L, 1L, request))
        .isInstanceOf(CdTemplateNotFoundException.class);
  }

  @Test
  @DisplayName("CD 템플릿 수정 실패 - 권한 없음")
  void updateTemplate_Unauthorized() {
    // given
    User otherUser = User.builder().id(2L).nickname("다른 사용자").build();
    MyCd otherMyCd = MyCd.builder().id(2L).user(otherUser).build();
    CdTemplate otherTemplate = CdTemplate.builder().myCd(otherMyCd).build();

    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.of(otherTemplate));

    // when & then
    assertThatThrownBy(() -> cdTemplateService.updateTemplate(1L, 1L, request))
        .isInstanceOf(UnauthorizedCdTemplateAccessException.class);
  }

  @Test
  @DisplayName("CD 템플릿 삭제 성공")
  void deleteTemplate_Success() {
    // given
    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.of(cdTemplate));

    // when
    cdTemplateService.deleteTemplate(1L, 1L);

    // then
    verify(cdTemplateRepository, times(1)).delete(cdTemplate);
  }

  @Test
  @DisplayName("CD 템플릿 삭제 실패 - 존재하지 않음")
  void deleteTemplate_NotFound() {
    // given
    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> cdTemplateService.deleteTemplate(1L, 1L))
        .isInstanceOf(CdTemplateNotFoundException.class);
  }

  @Test
  @DisplayName("CD 템플릿 삭제 실패 - 권한 없음")
  void deleteTemplate_Unauthorized() {
    // given
    User otherUser = User.builder().id(2L).nickname("다른 사용자").build();
    MyCd otherMyCd = MyCd.builder().id(2L).user(otherUser).build();
    CdTemplate otherTemplate = CdTemplate.builder().myCd(otherMyCd).build();

    BDDMockito.given(cdTemplateRepository.findByMyCdId(1L)).willReturn(Optional.of(otherTemplate));

    // when & then
    assertThatThrownBy(() -> cdTemplateService.deleteTemplate(1L, 1L))
        .isInstanceOf(UnauthorizedCdTemplateAccessException.class);
  }
}
