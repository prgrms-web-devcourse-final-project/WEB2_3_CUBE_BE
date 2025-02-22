package com.roome.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.user.entity.*;
import com.roome.domain.user.service.UserGenreService;
import com.roome.domain.user.temp.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserGenreController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserGenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserGenreService userGenreService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal mockUserPrincipal;

    @BeforeEach
    void setUp() {
        User mockUser = User
                .builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .nickname("TestUser")
                .profileImage("test.jpg")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("test123")
                .build();

        mockUserPrincipal = new UserPrincipal(mockUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(mockUserPrincipal, null,
                                                                                mockUserPrincipal.getAuthorities());
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("음악 감성 목록 조회 성공")
    void getMusicGenres_Success() throws Exception {
        mockMvc
                .perform(get("/api/users/music-genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자의 음악 감성 조회 성공")
    void getUserMusicGenres_Success() throws Exception {
        // given
        Long userId = 1L;
        List<MusicGenre> genres = Arrays.asList(MusicGenre.HIPHOP, MusicGenre.LOFI);

        when(userGenreService.getUserMusicGenres(userId)).thenReturn(genres);

        // when & then
        mockMvc
                .perform(get("/api/users/{userId}/music-genres", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("HIPHOP"))
                .andExpect(jsonPath("$[1]").value("LOFI"))
                .andDo(print());
    }

    @Test
    @DisplayName("음악 감성 추가 성공")
    void addMusicGenre_Success() throws Exception {
        // given
        String genre = "HIPHOP";

        // when & then
        mockMvc
                .perform(post("/api/users/music-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(genre))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("음악 감성 수정 성공")
    void updateMusicGenres_Success() throws Exception {
        // given
        List<String> genres = Arrays.asList("HIPHOP", "LOFI");

        // when & then
        mockMvc
                .perform(put("/api/users/music-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(objectMapper.writeValueAsString(genres)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("독서 취향 목록 조회 성공")
    void getBookGenres_Success() throws Exception {
        mockMvc
                .perform(get("/api/users/book-genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자의 독서 취향 조회 성공")
    void getUserBookGenres_Success() throws Exception {
        // given
        Long userId = 1L;
        List<BookGenre> genres = Arrays.asList(BookGenre.SF, BookGenre.MYSTERY);

        when(userGenreService.getUserBookGenres(userId)).thenReturn(genres);

        // when & then
        mockMvc
                .perform(get("/api/users/{userId}/book-genres", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("SF"))
                .andExpect(jsonPath("$[1]").value("MYSTERY"))
                .andDo(print());
    }

    @Test
    @DisplayName("독서 취향 추가 성공")
    void addBookGenre_Success() throws Exception {
        // given
        String genre = "SF";

        // when & then
        mockMvc
                .perform(post("/api/users/book-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(genre))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("독서 취향 수정 성공")
    void updateBookGenres_Success() throws Exception {
        // given
        List<String> genres = Arrays.asList("SF", "MYSTERY");

        // when & then
        mockMvc
                .perform(put("/api/users/book-genres")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(objectMapper.writeValueAsString(genres)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("추천 유저 조회 성공")
    void getRecommendedUsers_Success() throws Exception {
        // given
        Long userId = 1L;
        List<User> users = Arrays.asList(User
                                                 .builder()
                                                 .id(2L)
                                                 .email("user2@example.com")
                                                 .nickname("User2")
                                                 .build(), User
                                                 .builder()
                                                 .id(3L)
                                                 .email("user3@example.com")
                                                 .nickname("User3")
                                                 .build());

        when(userGenreService.getRecommendedUsers(userId)).thenReturn(users);

        // when & then
        mockMvc
                .perform(get("/api/users/{userId}/recommendations", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(3))
                .andDo(print());
    }
}