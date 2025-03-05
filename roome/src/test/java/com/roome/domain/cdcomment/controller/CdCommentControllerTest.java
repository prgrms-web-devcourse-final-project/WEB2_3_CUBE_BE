package com.roome.domain.cdcomment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.cdcomment.dto.CdCommentCreateRequest;
import com.roome.domain.cdcomment.dto.CdCommentListResponse;
import com.roome.domain.cdcomment.dto.CdCommentResponse;
import com.roome.domain.cdcomment.service.CdCommentService;
import com.roome.global.exception.ForbiddenException;
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
import static org.mockito.ArgumentMatchers.anyInt;
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

  @DisplayName("CD 댓글 추가 성공")
  @WithMockUser(username = "1")
  @Test
  void addComment_Success() throws Exception {
    CdCommentCreateRequest request = new CdCommentCreateRequest(220, "이 곡 최고네요!");
    CdCommentResponse response = createCdCommentResponse(1L, request);

    BDDMockito.given(cdCommentService.addComment(eq(1L), eq(1L), any(CdCommentCreateRequest.class)))
        .willReturn(response);

    mockMvc.perform(post("/api/my-cd/1/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("이 곡 최고네요!"))
        .andExpect(jsonPath("$.timestamp").value(220));
  }

  @DisplayName("CD 댓글 목록 조회 성공")
  @WithMockUser
  @Test
  void getComments_Success() throws Exception {
    CdCommentListResponse response = new CdCommentListResponse(
        List.of(createCdCommentResponse(1L, new CdCommentCreateRequest(220, "이 곡 최고네요!"))),
        0, 5, 1, 1
    );

    BDDMockito.given(cdCommentService.getComments(eq(1L), any(), anyInt(), anyInt()))
        .willReturn(response);

    System.out.println("Mock Response: " + objectMapper.writeValueAsString(response));

    mockMvc.perform(get("/api/my-cd/1/comments")
            .param("page", "0")
            .param("size", "5")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].content").value("이 곡 최고네요!"));
  }

  @DisplayName("CD 전체 댓글 목록 조회 성공")
  @WithMockUser
  @Test
  void getAllComments_Success() throws Exception {
    List<CdCommentResponse> response = List.of(
        createCdCommentResponse(1L, new CdCommentCreateRequest(220, "이 곡 최고네요!")),
        createCdCommentResponse(2L, new CdCommentCreateRequest(250, "이 곡도 좋네요!"))
    );

    BDDMockito.given(cdCommentService.getAllComments(eq(1L)))
        .willReturn(response);

    System.out.println("Mock Response: " + objectMapper.writeValueAsString(response));

    mockMvc.perform(get("/api/my-cd/1/comments/all")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].content").value("이 곡 최고네요!"))
        .andExpect(jsonPath("$[1].content").value("이 곡도 좋네요!"));
  }


  @DisplayName("CD 댓글 삭제 성공")
  @WithMockUser(username = "1")
  @Test
  void deleteComment_Success() throws Exception {
    mockMvc.perform(delete("/api/my-cd/1/comments/1")
            .with(csrf()))
        .andExpect(status().isNoContent());

    BDDMockito.verify(cdCommentService).deleteComment(eq(1L), eq(1L));
  }

  @DisplayName("CD 댓글 삭제 실패 (권한 없음)")
  @WithMockUser(username = "3")
  @Test
  void deleteComment_Unauthorized() throws Exception {
    BDDMockito.doThrow(new ForbiddenException("해당 댓글을 삭제할 권한이 없습니다."))
        .when(cdCommentService).deleteComment(eq(3L), eq(1L));

    mockMvc.perform(delete("/api/my-cd/1/comments/1")
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  private CdCommentResponse createCdCommentResponse(Long commentId,
      CdCommentCreateRequest request) {
    return new CdCommentResponse(
        commentId, 1L, 2L, "현구", request.getTimestamp(),
        request.getContent(), LocalDateTime.of(2024, 3, 5, 12, 0, 0)
    );
  }
}
