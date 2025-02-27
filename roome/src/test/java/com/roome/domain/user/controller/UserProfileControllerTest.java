package com.roome.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.user.temp.UserPrincipal;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // test 프로필 사용
@Transactional // 각 테스트 후 롤백
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserPrincipal userPrincipal;

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

        // 인증 설정
        userPrincipal = new UserPrincipal(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("프로필 조회 성공 - 통합 테스트")
    void getUserProfile_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/{userId}/profile", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(testUser.getId())))
                .andExpect(jsonPath("$.nickname").value("TestUser"))
                .andExpect(jsonPath("$.profileImage").value("test.jpg"))
                .andExpect(jsonPath("$.bio").value("안녕하세요. 테스트 유저입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 프로필 조회 실패 - 통합 테스트")
    void getUserProfile_NotFound() throws Exception {
        // 존재하지 않는 ID로 조회
        Long nonExistentUserId = 9999L;

        // when & then
        mockMvc.perform(get("/api/users/{userId}/profile", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 성공 - 통합 테스트")
    void updateProfile_Success() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio("프로필이 수정되었습니다.")
                .build();

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(testUser.getId())))
                .andExpect(jsonPath("$.nickname").value("UpdatedUser"))
                .andExpect(jsonPath("$.bio").value("프로필이 수정되었습니다."))
                .andDo(print());

        // DB에서 직접 확인 (선택사항)
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getNickname().equals("UpdatedUser");
        assert updatedUser.getBio().equals("프로필이 수정되었습니다.");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 닉네임 검증 실패 - 통합 테스트")
    void updateProfile_Fail_NicknameValidation() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname(" ")  // 공백 닉네임
                .bio("프로필이 수정되었습니다.")
                .build();

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 닉네임 부적합 패턴 테스트")
    void updateProfile_Fail_NicknameInvalidPattern() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("User@123")  // 특수문자 포함 닉네임
                .bio("프로필이 수정되었습니다.")
                .build();

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 자기소개 null 검증 - 통합 테스트")
    void updateProfile_Fail_BioValidation() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio(null)  // null bio
                .build();

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 자기소개 경계값(정확히 100자) 테스트")
    void updateProfile_Success_BioBoundary() throws Exception {
        // given - 정확히 100자인 bio
        String hundredCharBio = "a".repeat(100);
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio(hundredCharBio)
                .build();

        // when & then - 100자는 성공해야 함
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 실패 - 자기소개 길이 검증 - 통합 테스트")
    void updateProfile_Fail_BioLengthValidation() throws Exception {
        // given - 101자인 bio
        String tooLongBio = "a".repeat(101);
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio(tooLongBio)
                .build();

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}