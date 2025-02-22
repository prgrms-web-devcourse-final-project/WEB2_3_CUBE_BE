//package com.roome.domain.houseMate.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.roome.domain.houseMate.service.HousemateService;
//import com.roome.domain.user.entity.Provider;
//import com.roome.domain.user.entity.User;
//import com.roome.domain.user.repository.UserRepository;
//import com.roome.global.jwt.dto.JwtToken;
//import com.roome.global.jwt.service.JwtTokenProvider;
//import com.roome.domain.user.entity.Status;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.transaction.annotation.Transactional;
//
//
//import java.nio.file.attribute.UserPrincipal;
//import java.util.Collections;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//class HousemateControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private HousemateService housemateService;
//
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//
//    private String userToken;
//    private User testUser;
//    private User targetUser;
//    private UserPrincipal principal;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트 사용자 생성
//        testUser = userRepository.save(User.builder()
//                                           .email("test@example.com")
//                                           .name("TestUser")
//                                           .nickname("testUser")
//                                           .provider(Provider.GOOGLE)  // Provider enum 필수
//                                           .providerId("test123")      // providerId 필수
//                                           .status(Status.ONLINE)      // Status enum 필수
//                                           .build());
//
//        targetUser = userRepository.save(User.builder()
//                                             .email("target@example.com")
//                                             .name("User")
//                                             .nickname("targetUser")
//                                             .provider(Provider.GOOGLE)
//                                             .providerId("target123")
//                                             .status(Status.OFFLINE)
//                                             .build());
//
//        // UserPrincipal 생성
//        principal = () -> testUser.getEmail();
//
//        // Authentication 객체 생성
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                testUser.getEmail(), "", Collections.emptyList());
//
//        // JWT 토큰 생성
//        JwtToken token = jwtTokenProvider.createToken(authentication);
//        userToken = token.getAccessToken();
//
//    }
//
//    @Test
//    @DisplayName("하우스메이트 추가 테스트")
//    void addHousemateTest() throws Exception {
//        // when
//        ResultActions result = mockMvc.perform(post("/api/mates/follow/" + targetUser.getId())
//                                                       .header("Authorization", "Bearer " + userToken)
//                                                       .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        result.andDo(print())
//              .andExpect(status().isCreated());
//    }
//    @Test
//    @DisplayName("팔로잉 목록 조회 테스트")
//    void getFollowingTest() throws Exception {
//        // given
//        housemateService.addHousemate(testUser.getId(), targetUser.getId());
//
//        // when
//        ResultActions result = mockMvc.perform(get("/api/mates/following")
//                                                       .header("Authorization", "Bearer " + userToken)
//                                                       .param("limit", "20")
//                                                       .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        result
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
//    }
//
//    @Test
//    @DisplayName("팔로워 목록 조회 테스트")
//    void getFollowersTest() throws Exception {
//        // given
//        housemateService.addHousemate(targetUser.getId(), testUser.getId());
//
//        // when
//        ResultActions result = mockMvc.perform(get("/api/mates/followers")
//                                                       .header("Authorization", "Bearer " + userToken)
//                                                       .param("limit", "20")
//                                                       .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        result
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
//    }
//
//    @Test
//    @DisplayName("하우스메이트 삭제 테스트")
//    void removeHousemateTest() throws Exception {
//        // given
//        housemateService.addHousemate(testUser.getId(), targetUser.getId());
//
//        // when
//        ResultActions result = mockMvc.perform(delete("/api/mates/follow/" + targetUser.getId())
//                                                       .header("Authorization", "Bearer " + userToken)
//                                                       .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        result
//                .andDo(print())
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    @DisplayName("자기 자신을 하우스메이트로 추가할 수 없음")
//    void cannotAddSelfAsHousemate() throws Exception {
//        // when
//        ResultActions result = mockMvc.perform(post("/api/mates/follow/" + testUser.getId())
//                                                       .header("Authorization", "Bearer " + userToken)
//                                                       .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        result
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 사용자를 하우스메이트로 추가할 수 없음")
//    void cannotAddNonExistentUser() throws Exception {
//        // when
//        ResultActions result = mockMvc.perform(post("/api/mates/follow/999999")
//                                                       .header("Authorization", "Bearer " + userToken)
//                                                       .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        result
//                .andDo(print())
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("이미 하우스메이트인 사용자를 다시 추가할 수 없음")
//    void cannotAddExistingHousemate() throws Exception {
//        // given
//        housemateService.addHousemate(testUser.getId(), targetUser.getId());
//
//        // when
//        ResultActions result = mockMvc.perform(post("/api/mates/follow/" + targetUser.getId())
//                                                       .header("Authorization", "Bearer " + userToken)
//                                                       .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        result
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//    }
//}