package com.roome.domain.mycd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.exception.MyCdAlreadyExistsException;
import com.roome.domain.mycd.exception.MyCdNotFoundException;
import com.roome.domain.mycd.service.MyCdService;
import com.roome.global.exception.ErrorCode;
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

import static org.hamcrest.Matchers.containsString;
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
  }

  @DisplayName("CD 추가 실패 - 중복 추가")
  @WithMockUser(username = "1")
  @Test
  void addMyCd_Failure_AlreadyExists() throws Exception {
    MyCdCreateRequest request = createMyCdCreateRequest();

    BDDMockito.given(myCdService.addCdToMyList(eq(1L), any(MyCdCreateRequest.class)))
        .willThrow(new MyCdAlreadyExistsException());

    mockMvc.perform(post("/api/my-cd").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(ErrorCode.MYCD_ALREADY_EXISTS.getMessage())));
  }

  @DisplayName("내 CD 목록 조회 성공")
  @WithMockUser(username = "1")
  @Test
  void getMyCdList_Success() throws Exception {
    MyCdListResponse response = new MyCdListResponse(
        List.of(createMyCdResponse(1L, createMyCdCreateRequest())), 1L);

    BDDMockito.given(myCdService.getMyCdList(eq(1L), any(Long.class), any(Integer.class)))
        .willReturn(response);

    mockMvc.perform(get("/api/my-cd").param("size", "10").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @DisplayName("특정 CD 조회 성공")
  @WithMockUser(username = "1")
  @Test
  void getMyCd_Success() throws Exception {
    MyCdResponse response = createMyCdResponse(1L, createMyCdCreateRequest());

    BDDMockito.given(myCdService.getMyCd(eq(1L), eq(1L))).willReturn(response);

    mockMvc.perform(get("/api/my-cd/1")).andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Palette"));
  }

  @DisplayName("특정 CD 조회 실패 - 존재하지 않음")
  @WithMockUser(username = "1")
  @Test
  void getMyCd_Failure_NotFound() throws Exception {
    BDDMockito.given(myCdService.getMyCd(eq(1L), eq(999L))).willThrow(new MyCdNotFoundException());

    mockMvc.perform(get("/api/my-cd/999")).andExpect(status().isNotFound())
        .andExpect(content().string(containsString(ErrorCode.MYCD_NOT_FOUND.getMessage())));
  }

  @DisplayName("CD 삭제 성공")
  @WithMockUser(username = "1")
  @Test
  void deleteMyCd_Success() throws Exception {
    mockMvc.perform(delete("/api/my-cd")
            .param("myCdIds", "1")
            .param("myCdIds", "2")
            .param("myCdIds", "3")
            .with(csrf()))
        .andExpect(status().isNoContent());

    BDDMockito.verify(myCdService).delete(1L, List.of(1L, 2L, 3L));
  }

  @DisplayName("CD 삭제 실패 - 존재하지 않음")
  @WithMockUser(username = "1")
  @Test
  void deleteMyCd_Failure_NotFound() throws Exception {
    BDDMockito.doThrow(new MyCdNotFoundException()).when(myCdService)
        .delete(eq(1L), eq(List.of(999L)));

    // when & then: 요청 후 404 응답을 기대
    mockMvc.perform(delete("/api/my-cd").param("myCdIds", "999").with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString(ErrorCode.MYCD_NOT_FOUND.getMessage())));
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
