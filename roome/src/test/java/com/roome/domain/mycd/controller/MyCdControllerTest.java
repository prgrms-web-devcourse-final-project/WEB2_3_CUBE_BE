package com.roome.domain.mycd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.service.MyCdService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyCdController.class)
class MyCdControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MyCdService myCdService;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("CD 추가 성공")
  @WithMockUser(username = "1")
  @Test
  void addMyCd_Success() throws Exception {
    MyCdCreateRequest request = createMyCdCreateRequest();
    MyCdResponse response = createMyCdResponse(1L, request);

    BDDMockito.given(myCdService.addCdToMyList(eq(1L), any(MyCdCreateRequest.class)))
        .willReturn(response);

    mockMvc.perform(post("/api/my-cd")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Palette"))
        .andExpect(jsonPath("$.artist").value("IU"));

    BDDMockito.verify(myCdService).addCdToMyList(eq(1L), any(MyCdCreateRequest.class));
  }

  @Test
  @DisplayName("내 CD 목록 조회 성공 - targetUserId 없음 (자기 자신의 목록 조회)")
  @WithMockUser(username = "1")
  void getMyCdList_Success_NoTargetUserId() throws Exception {
    MyCdListResponse response = new MyCdListResponse(
        List.of(createMyCdResponse(1L, createMyCdCreateRequest())),
        1L, // nextCursor
        100L, // totalCount
        1L, // firstMyCdId
        10L  // lastMyCdId
    );

    // targetUserId가 없는 경우
    BDDMockito.given(myCdService.getMyCdList(eq(1L), any(String.class), any(Long.class), any(Integer.class)))
        .willReturn(response);

    mockMvc.perform(get("/api/my-cd")
            .param("size", "10")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // targetUserId가 없을 경우 userId가 최종적으로 사용되는지 확인
    BDDMockito.verify(myCdService).getMyCdList(eq(1L), eq(null), eq(null), eq(10));
  }

  @Test
  @DisplayName("다른 사용자의 CD 목록 조회 성공 - targetUserId 있음")
  @WithMockUser(username = "1")
  void getMyCdList_Success_WithTargetUserId() throws Exception {
    MyCdListResponse response = new MyCdListResponse(
        List.of(createMyCdResponse(1L, createMyCdCreateRequest())),
        2L, // nextCursor
        200L, // totalCount
        5L, // firstMyCdId
        15L  // lastMyCdId
    );

    // targetUserId가 있는 경우
    BDDMockito.given(myCdService.getMyCdList(eq(2L), any(String.class), any(Long.class), any(Integer.class)))
        .willReturn(response);

    mockMvc.perform(get("/api/my-cd")
            .param("targetUserId", "2")  // 다른 사용자 조회
            .param("size", "10")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // targetUserId가 있으면 그것이 사용되는지 확인
    BDDMockito.verify(myCdService).getMyCdList(eq(2L), eq(null), eq(null), eq(10));
  }

  @DisplayName("특정 CD 조회 성공")
  @WithMockUser(username = "1")
  @Test
  void getMyCd_Success() throws Exception {
    MyCdResponse response = createMyCdResponse(1L, createMyCdCreateRequest());

    BDDMockito.given(myCdService.getMyCd(eq(1L), eq(1L))).willReturn(response);

    mockMvc.perform(get("/api/my-cd/1")
            .param("targetUserId", "1")) // 필수값 추가
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Palette"));

    BDDMockito.verify(myCdService).getMyCd(eq(1L), eq(1L));
  }

  @DisplayName("CD 삭제 성공")
  @WithMockUser(username = "1")
  @Test
  void deleteMyCd_Success() throws Exception {
    mockMvc.perform(delete("/api/my-cd")
            .param("myCdIds", "1")
            .param("myCdIds", "2")
            .param("myCdIds", "3")
            .with(csrf()))  // CSRF 적용
        .andExpect(status().isNoContent());

    BDDMockito.verify(myCdService).delete(eq(1L), eq(List.of(1L, 2L, 3L)));
  }

  private MyCdCreateRequest createMyCdCreateRequest() {
    return new MyCdCreateRequest("Palette", "IU", "Palette", LocalDate.of(2019, 11, 1),
        List.of("K-Pop", "Ballad"), "https://example.com/image1.jpg",
        "https://youtube.com/watch?v=asdf5678", 215);
  }

  private MyCdResponse createMyCdResponse(Long myCdId, MyCdCreateRequest request) {
    return new MyCdResponse(myCdId, request.getTitle(), request.getArtist(), request.getAlbum(),
        request.getReleaseDate(), request.getGenres(), request.getCoverUrl(),
        request.getYoutubeUrl(), request.getDuration());
  }
}
