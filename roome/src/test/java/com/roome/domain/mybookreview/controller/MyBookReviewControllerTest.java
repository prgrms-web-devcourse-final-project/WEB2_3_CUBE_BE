package com.roome.domain.mybookreview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.mybookreview.entity.CoverColor;
import com.roome.domain.mybookreview.service.MyBookReviewService;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MyBookReviewController.class)
class MyBookReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyBookReviewService myBookReviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("서평을 작성할 수 있다.")
    @WithMockUser
    @Test
    void create() throws Exception {

        // given
        String title = "title";
        MyBookReviewCreateRequest request = createMyBookReviewCreateRequest(title);

        Long myBookReviewId = 1L;
        MyBookReviewResponse response = createMyBookReviewResponse(myBookReviewId, request);

        Long myBookId = 1L;
        given(myBookReviewService.create(1L, myBookId, request))
                .willReturn(response);

        // when // then
        mockMvc.perform(
                        post("/api/mybooks-review")
                                .param("myBookId", String.valueOf(myBookId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(myBookReviewId)))
                .andExpect(jsonPath("$.title").value(request.title()));
    }

    @DisplayName("서평을 조회할 수 있다.")
    @WithMockUser
    @Test
    void read() throws Exception {

        // given
        Long myBookReviewId = 1L;
        String title = "title";
        MyBookReviewResponse response = createMyBookReviewResponse(myBookReviewId, title);

        Long myBookId = 1L;
        given(myBookReviewService.read(myBookId))
                .willReturn(response);

        // when // then
        mockMvc.perform(
                        get("/api/mybooks-review")
                                .param("myBookId", String.valueOf(myBookId))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(myBookReviewId)))
                .andExpect(jsonPath("$.title").value(title));
    }

    @DisplayName("서평을 수정할 수 있다.")
    @WithMockUser
    @Test
    void update() throws Exception {

        // given
        String title = "new-title";
        MyBookReviewUpdateRequest request = createMyBookReviewUpdateRequest(title);

        Long myBookReviewId = 1L;
        MyBookReviewResponse response = createMyBookReviewResponse(myBookReviewId, title);

        given(myBookReviewService.update(1L, myBookReviewId, request))
                .willReturn(response);

        // when // then
        mockMvc.perform(
                        patch("/api/mybooks-review/" + myBookReviewId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(myBookReviewId)))
                .andExpect(jsonPath("$.title").value(request.title()));
    }

    @DisplayName("서평을 삭제할 수 있다.")
    @WithMockUser
    @Test
    void delete() throws Exception {

        // given // when // then
        Long myBookReviewId = 1L;
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/mybooks-review/" + myBookReviewId)
                                .with(csrf())
                )
                .andExpect(status().isOk());
        verify(myBookReviewService).delete(1L, myBookReviewId);
    }

    private MyBookReviewCreateRequest createMyBookReviewCreateRequest(String title) {
        return new MyBookReviewCreateRequest(
                title,
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                "BLUE"
        );
    }

    private MyBookReviewUpdateRequest createMyBookReviewUpdateRequest(String title) {
        return new MyBookReviewUpdateRequest(
                title,
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                "BLUE"
        );
    }


    private MyBookReviewResponse createMyBookReviewResponse(Long myBookReviewId, MyBookReviewCreateRequest request) {
        return new MyBookReviewResponse(
                myBookReviewId,
                request.title(),
                request.quote(),
                request.takeaway(),
                request.motivate(),
                request.freeFormText(),
                request.freeFormText(),
                CoverColor.valueOf(request.coverColor())
        );
    }

    private MyBookReviewResponse createMyBookReviewResponse(Long myBookReviewId, String title) {
        return new MyBookReviewResponse(
                myBookReviewId,
                title,
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                CoverColor.BLUE
        );
    }
}