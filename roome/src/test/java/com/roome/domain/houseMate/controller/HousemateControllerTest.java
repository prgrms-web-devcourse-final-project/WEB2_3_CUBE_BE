package com.roome.domain.houseMate.controller;

import com.roome.domain.houseMate.service.HousemateService;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HousemateControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HousemateService housemateService;

    private User testUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
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

        // SecurityContext 설정
        TestSecurityContextHolder.clearContext();
    }

    private ResultActions performWithAuthenticatedUser(MockMvc mockMvc, org.springframework.test.web.servlet.RequestBuilder requestBuilder) throws Exception {
        // UsernamePasswordAuthenticationToken 생성
        // 이 때 principal에 사용자 ID를 넣어줍니다
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser.getId(), // Principal로 직접 ID 사용
                null,
                Collections.emptyList()
        );

        // 시큐리티 컨텍스트에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        return mockMvc.perform(requestBuilder);
    }

    @Test
    @DisplayName("하우스메이트 추가 테스트")
    @WithMockUser
    void addHousemateTest() throws Exception {
        // 요청 생성
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                post("/mates/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isCreated()); // 201 응답 확인
    }

    @Test
    @DisplayName("팔로잉 목록 조회 테스트")
    @WithMockUser
    void getFollowingTest() throws Exception {
        // 사전 준비: testUser가 targetUser를 팔로우하도록 설정
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // 요청 수행
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                get("/mates/following")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
    }

    @Test
    @DisplayName("유효하지 않은 커서로 팔로잉 목록 조회 테스트")
    @WithMockUser
    void getFollowingWithInvalidCursorTest() throws Exception {
        // 음수 커서 값으로 요청
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                get("/mates/following")
                        .param("cursor", "-1")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 cursor 값입니다."));
    }

    @Test
    @DisplayName("유효하지 않은 limit으로 팔로잉 목록 조회 테스트")
    @WithMockUser
    void getFollowingWithInvalidLimitTest() throws Exception {
        // 범위를 벗어난 limit으로 요청
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                get("/mates/following")
                        .param("limit", "101")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("닉네임 검색으로 팔로잉 목록 조회 테스트")
    @WithMockUser
    void getFollowingWithNicknameSearchTest() throws Exception {
        // 사전 준비: testUser가 targetUser를 팔로우하도록 설정
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // 닉네임으로 검색 요청
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                get("/mates/following")
                        .param("nickname", "target")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
    }

    @Test
    @DisplayName("팔로워 목록 조회 테스트")
    @WithMockUser
    void getFollowersTest() throws Exception {
        // 사전 준비: targetUser가 testUser를 팔로우하도록 설정
        housemateService.addHousemate(targetUser.getId(), testUser.getId());

        // 요청 수행
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                get("/mates/followers")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
    }

    @Test
    @DisplayName("유효하지 않은 커서로 팔로워 목록 조회 테스트")
    @WithMockUser
    void getFollowersWithInvalidCursorTest() throws Exception {
        // 음수 커서 값으로 요청
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                get("/mates/followers")
                        .param("cursor", "-1")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 cursor 값입니다."));
    }


    @Test
    @DisplayName("닉네임 검색으로 팔로워 목록 조회 테스트")
    @WithMockUser
    void getFollowersWithNicknameSearchTest() throws Exception {
        // 사전 준비: targetUser가 testUser를 팔로우하도록 설정
        housemateService.addHousemate(targetUser.getId(), testUser.getId());

        // 닉네임으로 검색 요청
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                get("/mates/followers")
                        .param("nickname", "target")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.housemates[0].nickname").value(targetUser.getNickname()));
    }

    @Test
    @DisplayName("하우스메이트 삭제 테스트")
    @WithMockUser
    void removeHousemateTest() throws Exception {
        // 사전 준비: testUser가 targetUser를 팔로우하도록 설정
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // 요청 수행
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                delete("/mates/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 하우스메이트 삭제 시도 테스트")
    @WithMockUser
    void removeNonExistentHousemateTest() throws Exception {
        // 하우스메이트로 추가되지 않은 상태에서 삭제 시도
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                delete("/mates/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("하우스메이트로 추가되지 않은 사용자입니다."));
    }

    @Test
    @DisplayName("자기 자신을 하우스메이트로 추가할 수 없음")
    @WithMockUser
    void cannotAddSelfAsHousemate() throws Exception {
        // 자기 자신을 하우스메이트로 추가 시도
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                post("/mates/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.SELF_FOLLOW_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 하우스메이트로 추가할 수 없음")
    @WithMockUser
    void cannotAddNonExistentUser() throws Exception {
        // 존재하지 않는 사용자를 하우스메이트로 추가 시도
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                post("/mates/999999")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("이미 하우스메이트인 사용자를 다시 추가할 수 없음")
    @WithMockUser
    void cannotAddExistingHousemate() throws Exception {
        // 사전 준비: testUser가 targetUser를 팔로우하도록 설정
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // 이미 하우스메이트인 사용자를 다시 추가 시도
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                post("/mates/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 하우스메이트로 추가된 사용자입니다."));
    }

    @Test
    @DisplayName("유효하지 않은 targetId로 하우스메이트 추가 실패")
    @WithMockUser
    void cannotAddHousemateWithInvalidTargetId() throws Exception {
        // 유효하지 않은 targetId로 하우스메이트 추가 시도
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                post("/mates/-1")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("유효하지 않은 targetId로 하우스메이트 삭제 실패")
    @WithMockUser
    void cannotRemoveHousemateWithInvalidTargetId() throws Exception {
        // 유효하지 않은 targetId로 하우스메이트 삭제 시도
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                delete("/mates/-1")
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("자기 자신을 하우스메이트에서 삭제할 수 없음")
    @WithMockUser
    void cannotRemoveSelfAsHousemate() throws Exception {
        // 자기 자신을 하우스메이트에서 삭제 시도
        ResultActions result = performWithAuthenticatedUser(mockMvc,
                delete("/mates/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        // 결과 확인
        result
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("자기 자신을 하우스메이트로 추가할 수 없습니다."));
    }
}