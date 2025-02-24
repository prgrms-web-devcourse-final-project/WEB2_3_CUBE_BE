package com.roome.domain.houseMate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.houseMate.dto.HousemateListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(MockHouseMateController.class)
@AutoConfigureMockMvc(addFilters = false)
class MockHouseMateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("팔로워 목록 조회 API 테스트")
    void getFollowersTest() throws Exception {
        // given
        String url = "/mock/mates/followers";

        // when & then
        MvcResult result = mockMvc.perform(get(url)
                                                   .param("limit", "20")
                                                   .contentType(MediaType.APPLICATION_JSON))
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("$.housemates").isArray())
                                  .andExpect(jsonPath("$.housemates.length()").value(20))
                                  .andExpect(jsonPath("$.housemates[0].userId").value(1))
                                  .andExpect(jsonPath("$.housemates[0].nickname").value("사용자 1"))
                                  .andExpect(jsonPath("$.hasNext").value(true))
                                  .andReturn();

        String content = result.getResponse().getContentAsString();
        HousemateListResponse response = objectMapper.readValue(content, HousemateListResponse.class);
        assertEquals(20, response.getHousemates().size());
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 닉네임 필터링 테스트")
    void getFollowersWithNicknameTest() throws Exception {
        // given
        String url = "/mock/mates/followers";
        String nickname = "사용자 1"; // Mock 데이터에 있는 닉네임 패턴 사용

        // when & then
        mockMvc.perform(get(url)
                                .param("nickname", nickname)
                                .param("limit", "20")
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.housemates").isArray())
               .andExpect(jsonPath("$.housemates[0].nickname").value("사용자 1"))
               .andExpect(jsonPath("$.housemates[0].bio").value("사용자 1의 상태메시지입니다."));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 커서 기반 페이징 테스트")
    void getFollowersWithCursorTest() throws Exception {
        // given
        String url = "/mock/mates/followers";
        String cursor = "20"; // 20번째 유저 이후의 데이터를 요청

        // when & then
        mockMvc.perform(get(url)
                                .param("cursor", cursor)
                                .param("limit", "20")
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.housemates[0].userId").value(21))
               .andExpect(jsonPath("$.housemates[0].nickname").value("사용자 21"));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 API 테스트")
    void getFollowingTest() throws Exception {
        // given
        String url = "/mock/mates/following";

        // when & then
        mockMvc.perform(get(url)
                                .param("limit", "20")
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.housemates").isArray())
               .andExpect(jsonPath("$.housemates.length()").value(20))
               .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("팔로우 추가 API 테스트")
    void addHousemateTest() throws Exception {
        // given
        String url = "/mock/mates/follow/1";

        // when & then
        mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("팔로우 삭제 API 테스트")
    void removeHousemateTest() throws Exception {
        // given
        String url = "/mock/mates/follow/1";

        // when & then
        mockMvc.perform(delete(url)
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNoContent());
    }
}