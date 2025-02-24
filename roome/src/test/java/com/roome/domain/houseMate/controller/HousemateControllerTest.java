package com.roome.domain.houseMate.controller;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.houseMate.service.HousemateService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HousemateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired // 실제 서비스 사용
    private HousemateService housemateService;

    private User testUser;
    private User targetUser;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        // 1. 두 사용자 생성
        testUser = userRepository.save(User
                                               .builder()
                                               .email("test@example.com")
                                               .name("TestUser")
                                               .nickname("testUser")
                                               .provider(Provider.GOOGLE)
                                               .providerId("test123")
                                               .status(Status.ONLINE)
                                               .build());

        targetUser = userRepository.save(User
                                                 .builder()
                                                 .email("target@example.com")
                                                 .name("User")
                                                 .nickname("targetUser")
                                                 .provider(Provider.GOOGLE)
                                                 .providerId("target123")
                                                 .status(Status.OFFLINE)
                                                 .build());

        // 2. 인증 설정 - OAuth2UserPrincipal 대신 기본적인 인증 객체 사용
        OAuth2UserPrincipal principal = mock(OAuth2UserPrincipal.class);
        when(principal.getId()).thenReturn(testUser.getId());

        auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }


    @Test
    @DisplayName("하우스메이트 추가 테스트")
    void addHousemateTest() throws Exception {
        // 3. 가장 기본적인 요청
        ResultActions result = mockMvc.perform(
                post("/api/mates/follow/" + targetUser.getId())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON));

        // 4. 결과 확인
        result
                .andDo(print())
                .andExpect(status().isCreated()); // 201 응답 확인
    }

    @Test
    @DisplayName("팔로잉 목록 조회 테스트")
    void getFollowingTest() throws Exception {
        // 사전 준비: testUser가 targetUser를 팔로우하도록 설정
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // when - authentication() 사용
        ResultActions result = mockMvc.perform(get("/api/mates/following")
                                                       .with(authentication(auth))
                                                       .param("limit", "20")
                                                       .contentType(MediaType.APPLICATION_JSON));

        // then
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
    }

    @Test
    @DisplayName("팔로워 목록 조회 테스트")
    void getFollowersTest() throws Exception {
        // 사전 준비: targetUser가 testUser를 팔로우하도록 설정
        housemateService.addHousemate(targetUser.getId(), testUser.getId());

        // when - authentication() 사용
        ResultActions result = mockMvc.perform(get("/api/mates/followers")
                                                       .with(authentication(auth))
                                                       .param("limit", "20")
                                                       .contentType(MediaType.APPLICATION_JSON));

        // then
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
    }

    @Test
    @DisplayName("하우스메이트 삭제 테스트")
    void removeHousemateTest() throws Exception {
        // 사전 준비: testUser가 targetUser를 팔로우하도록 설정
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // when - authentication() 사용
        ResultActions result = mockMvc.perform(delete("/api/mates/follow/" + targetUser.getId())
                                                       .with(authentication(auth))
                                                       .contentType(MediaType.APPLICATION_JSON));

        // then
        result
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("자기 자신을 하우스메이트로 추가할 수 없음")
    void cannotAddSelfAsHousemate() throws Exception {
        // when - authentication() 사용
        ResultActions result = mockMvc.perform(post("/api/mates/follow/" + testUser.getId())
                                                       .with(authentication(auth))
                                                       .contentType(MediaType.APPLICATION_JSON));

        // then
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 하우스메이트로 추가할 수 없음")
    void cannotAddNonExistentUser() throws Exception {
        // when - authentication() 사용
        ResultActions result = mockMvc.perform(post("/api/mates/follow/999999")
                                                       .with(authentication(auth))
                                                       .contentType(MediaType.APPLICATION_JSON));

        // then
        result
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이미 하우스메이트인 사용자를 다시 추가할 수 없음")
    void cannotAddExistingHousemate() throws Exception {
        // 사전 준비: testUser가 targetUser를 팔로우하도록 설정
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // when - authentication() 사용
        ResultActions result = mockMvc.perform(post("/api/mates/follow/" + targetUser.getId())
                                                       .with(authentication(auth))
                                                       .contentType(MediaType.APPLICATION_JSON));

        // then
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}