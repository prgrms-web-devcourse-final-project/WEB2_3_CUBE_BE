package com.roome.domain.mybook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.mybook.service.MyBookService;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = MyBookController.class)
class MyBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyBookService myBookService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("도서를 등록할 수 있다.")
    @WithMockUser
    @Test
    void create() throws Exception {

        // given
        String title = "title";
        List<String> genreNames = List.of("웹", "IT");
        MyBookCreateRequest request = createMyBookCreateRequest(1213214432L, title, genreNames);

        Long myBookId = 1L;
        MyBookResponse response = createMyBookResponse(myBookId, request);

        Long roomOwnerId = 1L;
        given(myBookService.create(1L, roomOwnerId, request))
                .willReturn(response);

        // when // then
        mockMvc.perform(
                        post("/api/mybooks")
                                .param("userId", String.valueOf(roomOwnerId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(myBookId))
                .andExpect(jsonPath("$.title").value(request.title()))
                .andExpect(jsonPath("$.genreNames", hasSize(2)))
                .andExpect(jsonPath("$.genreNames", containsInAnyOrder(request.genreNames().get(0), request.genreNames().get(1))));
    }

    @DisplayName("도서의 상세 정보를 조회할 수 있다.")
    @WithMockUser
    @Test
    void read() throws Exception {

        // given
        String title = "title";
        List<String> genreNames = List.of("웹", "IT");
        Long myBookId = 1L;
        MyBookResponse response = createMyBookResponse(myBookId, title, genreNames);

        given(myBookService.read(myBookId))
                .willReturn(response);

        // when // then
        mockMvc.perform(
                        get("/api/mybooks/" + myBookId)
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.genreNames", hasSize(2)))
                .andExpect(jsonPath("$.genreNames", containsInAnyOrder("웹", "IT")));
    }

    @DisplayName("등록 도서 목록의 첫 번째 페이지를 조회할 수 있다.")
    @WithMockUser
    @Test
    void readAllFirstPage() throws Exception {

        // given
        String title = "title";
        List<String> genreNames = List.of("웹", "IT");
        MyBooksResponse response = createMyBooksResponse(
                List.of(
                        createMyBookResponse(5L, title, genreNames),
                        createMyBookResponse(4L, title, genreNames)
                ),
                5L
        );

        Long roomOwnerId = 1L;
        Long pageSize = 2L;
        given(myBookService.readAll(roomOwnerId, pageSize, null))
                .willReturn(response);

        // when // then
        mockMvc.perform(
                        get("/api/mybooks")
                                .param("userId", String.valueOf(roomOwnerId))
                                .param("pageSize", String.valueOf(pageSize))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myBooks", hasSize(2)))
                .andExpect(jsonPath("$.count").value(5));
    }

    @DisplayName("등록 도서 목록의 두 번째 페이지를 조회할 수 있다.")
    @WithMockUser
    @Test
    void readAllAfterFirstPage() throws Exception {

        // given
        String title = "title";
        List<String> genreNames = List.of("웹", "IT");
        MyBooksResponse response = createMyBooksResponse(
                List.of(
                        createMyBookResponse(3L, title, genreNames),
                        createMyBookResponse(2L, title, genreNames)
                ),
                5L
        );

        Long roomOwnerId = 1L;
        Long pageSize = 2L;
        Long lastMyBookId = 4L;
        given(myBookService.readAll(roomOwnerId, pageSize, lastMyBookId))
                .willReturn(response);

        // when // then
        mockMvc.perform(
                        get("/api/mybooks")
                                .param("userId", String.valueOf(roomOwnerId))
                                .param("pageSize", String.valueOf(pageSize))
                                .param("lastMyBookId", String.valueOf(lastMyBookId))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myBooks", hasSize(2)))
                .andExpect(jsonPath("$.count").value(5));
    }

    @DisplayName("등록 도서를 삭제할 수 있다.")
    @WithMockUser
    @Test
    void delete() throws Exception {

        // given // when // then
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/mybooks")
                                .param("userId", String.valueOf(1L))
                                .param("myBookIds", "1,2,3")
                                .with(csrf())
                )
                .andExpect(status().isOk());
        verify(myBookService).delete(1L, 1L, "1,2,3");
    }

    private MyBookCreateRequest createMyBookCreateRequest(Long isbn, String title, List<String> genreNames) {
        return new MyBookCreateRequest(
                isbn,
                title,
                "author",
                "publisher",
                LocalDate.of(2025, 1, 1),
                "image.jpg",
                genreNames,
                321L
        );
    }

    private MyBookResponse createMyBookResponse(Long myBookId, MyBookCreateRequest request) {
        return new MyBookResponse(
                myBookId,
                request.title(),
                request.author(),
                request.publisher(),
                request.publishedDate(),
                request.imageUrl(),
                request.genreNames(),
                request.page()
        );
    }

    private MyBookResponse createMyBookResponse(Long myBookId, String title, List<String> genreNames) {
        return new MyBookResponse(
                myBookId,
                title,
                "author",
                "publisher",
                LocalDate.of(2025, 1, 1),
                "image.url",
                genreNames,
                321L
        );
    }

    private MyBooksResponse createMyBooksResponse(List<MyBookResponse> myBookResponses, Long totalMyBookCount) {
        return new MyBooksResponse(
                myBookResponses,
                totalMyBookCount
        );
    }
}