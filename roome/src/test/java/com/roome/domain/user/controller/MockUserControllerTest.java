package com.roome.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.user.dto.request.MockUpdateProfileRequest;
import com.roome.domain.user.entity.BookGenre;
import com.roome.domain.user.entity.MusicGenre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockUserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Spring Security 필터 비활성화
class MockUserControllerTest {

    private static final String BASE_URL = "/mock/users";
    private static final Long MOCK_USER_ID = 999L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getMockUserProfile_success() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc
                .perform(get(BASE_URL + "/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nickname").value("Mock User " + userId))
                .andExpect(jsonPath("$.profileImage").exists())
                .andExpect(jsonPath("$.bio").exists())
                .andExpect(jsonPath("$.bookGenres").isArray())
                .andExpect(jsonPath("$.musicGenres").isArray())
                .andExpect(jsonPath("$.similarUser").isArray());
    }

    @Test
    @DisplayName("프로필 수정 - 성공")
    void updateMockUserProfile_success() throws Exception {
        // given
        MockUpdateProfileRequest request = MockUpdateProfileRequest
                .builder()
                .nickname("Updated Nickname")
                .bio("Updated bio")
                .build();

        // when & then
        mockMvc
                .perform(patch(BASE_URL + "/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MOCK_USER_ID.toString()))
                .andExpect(jsonPath("$.nickname").value(request.getNickname()))
                .andExpect(jsonPath("$.bio").value(request.getBio()));
    }

    @Test
    @DisplayName("음악 감성 목록 조회 - 성공")
    void getMusicGenres_success() throws Exception {
        // when & then
        mockMvc
                .perform(get(BASE_URL + "/music-genres"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("음악 감성 추가 - 성공")
    void addMusicGenre_success() throws Exception {
        // given
        String genre = MusicGenre.values()[0].name();

        // when & then
        mockMvc
                .perform(post(BASE_URL + "/music-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(genre))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(MOCK_USER_ID))
                .andExpect(jsonPath("$.genre").value(genre));
    }

    @Test
    @DisplayName("음악 감성 수정 - 성공")
    void updateMusicGenres_success() throws Exception {
        // given
        List<String> genres = List.of(MusicGenre.values()[0].name(), MusicGenre.values()[1].name());

        // when & then
        mockMvc
                .perform(put(BASE_URL + "/music-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(objectMapper.writeValueAsString(genres)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(MOCK_USER_ID))
                .andExpect(jsonPath("$[0].genre").value(genres.get(0)))
                .andExpect(jsonPath("$[1].genre").value(genres.get(1)));
    }

    @Test
    @DisplayName("독서 취향 목록 조회 - 성공")
    void getBookGenres_success() throws Exception {
        // when & then
        mockMvc
                .perform(get(BASE_URL + "/book-genres"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("독서 취향 추가 - 성공")
    void addBookGenre_success() throws Exception {
        // given
        String genre = BookGenre.values()[0].name();

        // when & then
        mockMvc
                .perform(post(BASE_URL + "/book-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(genre))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(MOCK_USER_ID))
                .andExpect(jsonPath("$.genre").value(genre));
    }

    @Test
    @DisplayName("독서 취향 수정 - 성공")
    void updateBookGenres_success() throws Exception {
        // given
        List<String> genres = List.of(BookGenre.values()[0].name(), BookGenre.values()[1].name());

        // when & then
        mockMvc
                .perform(put(BASE_URL + "/book-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(objectMapper.writeValueAsString(genres)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(MOCK_USER_ID))
                .andExpect(jsonPath("$[0].genre").value(genres.get(0)))
                .andExpect(jsonPath("$[1].genre").value(genres.get(1)));
    }

    @Test
    @DisplayName("추천 유저 조회 - 성공")
    void getRecommendedUsers_success() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc
                .perform(get(BASE_URL + "/{userId}/recommendations", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].nickname").exists())
                .andExpect(jsonPath("$[0].profileImage").exists());
    }
}