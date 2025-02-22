package com.roome.domain.cdcomment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.service.CdCommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CdCommentController.class)
@AutoConfigureMockMvc
class CdCommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CdCommentService cdCommentService;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("댓글 추가 성공")
  @WithMockUser
  @Test
  void addComment_Success() throws Exception {
    CdCommentCreateRequest request = createCdCommentCreateRequest();
    CdCommentResponse response = createCdCommentResponse(1L, request);

    BDDMockito.given(cdCommentService.addComment(any(Long.class), any(Long.class), any(CdCommentCreateRequest.class)))
        .willReturn(response);

    mockMvc.perform(post("/api/mycd/1/comment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("이 곡 최고네요!"))
        .andExpect(jsonPath("$.timestamp").value("03:40"));
  }

  @DisplayName("CD의 댓글 목록 조회 성공")
  @WithMockUser
  @Test
  void getComments_Success() throws Exception {
    CdCommentListResponse response = new CdCommentListResponse(
        List.of(createCdCommentResponse(1L, createCdCommentCreateRequest())), 0, 5, 1, 1
    );

    BDDMockito.given(cdCommentService.getComments(eq(1L), any(Integer.class), any(Integer.class)))
        .willReturn(response);

    mockMvc.perform(get("/api/mycd/1/comments")
            .param("page", "0")
            .param("size", "5")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].content").value("이 곡 최고네요!"));
  }

  @DisplayName("CD 댓글 검색 성공")
  @WithMockUser
  @Test
  void searchComments_Success() throws Exception {
    CdCommentListResponse response = new CdCommentListResponse(
        List.of(createCdCommentResponse(1L, createCdCommentCreateRequest())), 0, 5, 1, 1
    );

    BDDMockito.given(cdCommentService.searchComments(eq(1L), eq("최고"), any(Integer.class), any(Integer.class)))
        .willReturn(response);

    mockMvc.perform(get("/api/mycd/1/comments/search")
            .param("query", "최고")
            .param("page", "0")
            .param("size", "5")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].content").value("이 곡 최고네요!"));
  }

  @DisplayName("CD 댓글 삭제 성공")
  @WithMockUser
  @Test
  void deleteComment_Success() throws Exception {
    mockMvc.perform(delete("/api/mycd/comments/1")
            .with(csrf()))
        .andExpect(status().isNoContent());

    BDDMockito.verify(cdCommentService).deleteComment(1L);
  }

  @DisplayName("CD 댓글 다중 삭제 성공")
  @WithMockUser
  @Test
  void deleteMultipleComments_Success() throws Exception {
    mockMvc.perform(delete("/api/mycd/comments")
            .param("commentIds", "1", "2", "3")
            .with(csrf()))
        .andExpect(status().isNoContent());

    BDDMockito.verify(cdCommentService).deleteMultipleComments(List.of(1L, 2L, 3L));
  }

  private CdCommentCreateRequest createCdCommentCreateRequest() {
    return new CdCommentCreateRequest("03:40", "이 곡 최고네요!");
  }

  private CdCommentResponse createCdCommentResponse(Long commentId, CdCommentCreateRequest request) {
    return new CdCommentResponse(
        commentId, 1L, 2L, "현구", request.getTimestamp(),
        request.getContent(), LocalDateTime.now()
    );
  }
}
