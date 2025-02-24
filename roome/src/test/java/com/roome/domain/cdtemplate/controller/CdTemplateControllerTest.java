package com.roome.domain.cdtemplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.cdtemplate.dto.CdTemplateRequest;
import com.roome.domain.cdtemplate.dto.CdTemplateResponse;
import com.roome.domain.cdtemplate.service.CdTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CdTemplateController.class)
class CdTemplateControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CdTemplateService cdTemplateService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("CD 템플릿 생성 성공")
  @WithMockUser
  void createTemplate_Success() throws Exception {
    CdTemplateRequest request = createCdTemplateRequest();
    CdTemplateResponse response = createCdTemplateResponse(1L, request);

    BDDMockito.given(cdTemplateService.createTemplate(eq(1L), eq(1L), any(CdTemplateRequest.class)))
        .willReturn(response);

    mockMvc.perform(post("/api/my-cd/1/template")
            .param("userId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment1").value("CD를 듣게 된 계기"))
        .andExpect(jsonPath("$.comment2").value("CD에서 가장 좋았던 부분"));
  }

  @Test
  @DisplayName("CD 템플릿 조회 성공")
  @WithMockUser
  void getTemplate_Success() throws Exception {
    CdTemplateResponse response = createCdTemplateResponse(1L, createCdTemplateRequest());

    BDDMockito.given(cdTemplateService.getTemplate(eq(1L))).willReturn(response);

    mockMvc.perform(get("/api/my-cd/1/template")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment1").value("CD를 듣게 된 계기"));
  }

  @Test
  @DisplayName("CD 템플릿 수정 성공")
  @WithMockUser
  void updateTemplate_Success() throws Exception {
    CdTemplateRequest request = createCdTemplateRequest();
    CdTemplateResponse response = createCdTemplateResponse(1L, request);

    BDDMockito.given(cdTemplateService.updateTemplate(eq(1L), eq(1L), any(CdTemplateRequest.class)))
        .willReturn(response);

    mockMvc.perform(patch("/api/my-cd/1/template")
            .param("userId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment2").value("CD에서 가장 좋았던 부분"));
  }

  @Test
  @DisplayName("CD 템플릿 삭제 성공")
  @WithMockUser
  void deleteTemplate_Success() throws Exception {
    mockMvc.perform(delete("/api/my-cd/1/template")
            .param("userId", "1")
            .with(csrf()))
        .andExpect(status().isNoContent());

    BDDMockito.verify(cdTemplateService).deleteTemplate(1L, 1L);
  }

  private CdTemplateRequest createCdTemplateRequest() {
    return new CdTemplateRequest(
        "CD를 듣게 된 계기",
        "CD에서 가장 좋았던 부분",
        "CD를 들으며 느낀 감정",
        "자주 듣는 상황"
    );
  }

  private CdTemplateResponse createCdTemplateResponse(Long templateId, CdTemplateRequest request) {
    return new CdTemplateResponse(
        templateId, 1L,
        request.getComment1(),
        request.getComment2(),
        request.getComment3(),
        request.getComment4()
    );
  }
}
