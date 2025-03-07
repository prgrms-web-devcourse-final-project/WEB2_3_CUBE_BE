package com.roome.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.houseMate.entity.AddedHousemate;
import com.roome.domain.houseMate.repository.HousemateRepository;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HousemateRepository housemateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User friendUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 및 저장
        testUser = User.builder()
                .email("test@example.com")
                .name("Test User")
                .nickname("TestUser")
                .profileImage("test.jpg")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("test123")
                .bio("안녕하세요. 테스트 유저입니다.")
                .build();

        // 실제 DB에 저장
        testUser = userRepository.save(testUser);

        // 친구 테스트용 사용자 생성
        friendUser = User.builder()
                .email("friend@example.com")
                .name("Friend User")
                .nickname("FriendUser")
                .profileImage("friend.jpg")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("friend123")
                .bio("안녕하세요. 친구 유저입니다.")
                .build();

        // 실제 DB에 저장
        friendUser = userRepository.save(friendUser);

        // 인증 설정 - 사용자 ID를 Principal로 사용
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        housemateRepository.deleteAll();
        userRepository.deleteAll();
    }

    // AuthenticatedUser 어노테이션을 사용한 인증된 요청을 수행하는 헬퍼 메서드
    private ResultActions performWithAuthenticatedUser(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        // SecurityContext에 인증 정보가 이미 설정되어 있으므로 바로 수행
        return mockMvc.perform(requestBuilder);
    }

    @Test
    @DisplayName("자기 자신의 프로필 조회 성공 - 통합 테스트")
    @WithMockUser
    void getUserProfile_Self_Success() throws Exception {
        // when & then
        performWithAuthenticatedUser(get("/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(testUser.getId())))
                .andExpect(jsonPath("$.nickname").value("TestUser"))
                .andExpect(jsonPath("$.profileImage").value("test.jpg"))
                .andExpect(jsonPath("$.bio").value("안녕하세요. 테스트 유저입니다."))
                .andExpect(jsonPath("$.myProfile").value(true))
                .andExpect(jsonPath("$.following").value(false))  // isFollowing으로 변경
                .andDo(print());
    }

    @Test
    @DisplayName("친구가 아닌 다른 사용자 프로필 조회 성공 - 통합 테스트")
    @WithMockUser
    void getUserProfile_NotFriend_Success() throws Exception {
        // when & then
        performWithAuthenticatedUser(get("/users/{userId}/profile", friendUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(friendUser.getId())))
                .andExpect(jsonPath("$.nickname").value("FriendUser"))
                .andExpect(jsonPath("$.profileImage").value("friend.jpg"))
                .andExpect(jsonPath("$.bio").value("안녕하세요. 친구 유저입니다."))
                .andExpect(jsonPath("$.myProfile").value(false))
                .andExpect(jsonPath("$.following").value(false))  // isFollowing으로 변경
                .andDo(print());
    }

    @Test
    @DisplayName("친구인 다른 사용자 프로필 조회 성공 - 통합 테스트")
    @WithMockUser
    void getUserProfile_Friend_Success() throws Exception {
        // given: 친구 관계 설정
        AddedHousemate housemate = AddedHousemate.builder()
                .userId(testUser.getId())
                .addedId(friendUser.getId())
                .build();
        housemateRepository.save(housemate);

        // when & then
        performWithAuthenticatedUser(get("/users/{userId}/profile", friendUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(friendUser.getId())))
                .andExpect(jsonPath("$.nickname").value("FriendUser"))
                .andExpect(jsonPath("$.profileImage").value("friend.jpg"))
                .andExpect(jsonPath("$.bio").value("안녕하세요. 친구 유저입니다."))
                .andExpect(jsonPath("$.myProfile").value(false))
                .andExpect(jsonPath("$.following").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 프로필 조회 실패 - 통합 테스트")
    @WithMockUser
    void getUserProfile_NotFound() throws Exception {
        // 존재하지 않는 ID로 조회
        Long nonExistentUserId = 9999L;

        // when & then
        performWithAuthenticatedUser(get("/users/{userId}/profile", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 성공 - 통합 테스트")
    @WithMockUser
    void updateProfile_Success() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio("프로필이 수정되었습니다.")
                .build();

        // when & then
        performWithAuthenticatedUser(patch("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(testUser.getId())))
                .andExpect(jsonPath("$.nickname").value("UpdatedUser"))
                .andExpect(jsonPath("$.bio").value("프로필이 수정되었습니다."))
                .andExpect(jsonPath("$.myProfile").value(true))
                .andExpect(jsonPath("$.following").value(false))  // isFollowing으로 변경
                .andDo(print());

        // DB에서 직접 확인 (선택사항)
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getNickname().equals("UpdatedUser");
        assert updatedUser.getBio().equals("프로필이 수정되었습니다.");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 닉네임 검증 실패 - 통합 테스트")
    @WithMockUser
    void updateProfile_Fail_NicknameValidation() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname(" ")  // 공백 닉네임
                .bio("프로필이 수정되었습니다.")
                .build();

        // when & then
        performWithAuthenticatedUser(patch("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 닉네임 부적합 패턴 테스트")
    @WithMockUser
    void updateProfile_Fail_NicknameInvalidPattern() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("User@123")  // 특수문자 포함 닉네임
                .bio("프로필이 수정되었습니다.")
                .build();

        // when & then
        performWithAuthenticatedUser(patch("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 자기소개 null 검증 - 통합 테스트")
    @WithMockUser
    void updateProfile_Fail_BioValidation() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio(null)  // null bio
                .build();

        // when & then
        performWithAuthenticatedUser(patch("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 자기소개 경계값(정확히 100자) 테스트")
    @WithMockUser
    void updateProfile_Success_BioBoundary() throws Exception {
        // given - 정확히 100자인 bio
        String hundredCharBio = "a".repeat(100);
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio(hundredCharBio)
                .build();

        // when & then - 100자는 성공해야 함
        performWithAuthenticatedUser(patch("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 자기소개 길이 검증 - 통합 테스트")
    @WithMockUser
    void updateProfile_Fail_BioLengthValidation() throws Exception {
        // given - 101자인 bio
        String tooLongBio = "a".repeat(101);
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio(tooLongBio)
                .build();

        // when & then
        performWithAuthenticatedUser(patch("/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}