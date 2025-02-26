package com.roome.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.service.UserProfileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal mockUserPrincipal;

    @BeforeEach
    void setUp() {
        User mockUser = User.builder()
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
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                mockUserPrincipal, null, mockUserPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getUserProfile_Success() throws Exception {
        // given
        Long userId = 1L;
        UserProfileResponse response = UserProfileResponse.builder()
                                                          .id(String.valueOf(userId))
                                                          .nickname("TestUser")
                                                          .profileImage("test.jpg")
                                                          .bio("안녕하세요. 테스트 유저입니다.")
                                                          .build();

        when(userProfileService.getUserProfile(eq(userId), any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/{userId}/profile", userId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(String.valueOf(userId)))
               .andExpect(jsonPath("$.nickname").value("TestUser"))
               .andExpect(jsonPath("$.profileImage").value("test.jpg"))
               .andExpect(jsonPath("$.bio").value("안녕하세요. 테스트 유저입니다."))
               .andDo(print());
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_Success() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                                           .nickname("UpdatedUser")
                                                           .bio("프로필이 수정되었습니다.")
                                                           .build();

        UserProfileResponse response = UserProfileResponse.builder()
                                                          .id("1")
                                                          .nickname("UpdatedUser")
                                                          .profileImage("updated.jpg")
                                                          .bio("프로필이 수정되었습니다.")
                                                          .build();

        when(userProfileService.updateProfile(any(), any())).thenReturn(response);

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.nickname").value("UpdatedUser"))
               .andExpect(jsonPath("$.profileImage").value("updated.jpg"))
               .andExpect(jsonPath("$.bio").value("프로필이 수정되었습니다."))
               .andDo(print());
    }
    @Test
    @DisplayName("프로필 수정 실패 - 닉네임이 검증 실패")
    void updateProfile_Fail_NicknameValidation() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                                           .nickname(" ")
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
    @DisplayName("프로필 수정 실패 - 자기소개 검증 실패")
    void updateProfile_Fail_BioValidation() throws Exception {
        // given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                                                           .nickname("UpdatedUser")
                                                           .bio(null)
                                                           .build();

        // when & then
        mockMvc.perform(patch("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }
}