package com.roome.domain.houseMate.service;

import com.roome.domain.houseMate.dto.HousemateInfo;
import com.roome.domain.houseMate.dto.HousemateListResponse;
import com.roome.domain.houseMate.entity.AddedHousemate;
import com.roome.domain.houseMate.repository.AddedHousemateRepository;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousemateServiceTest {

    @InjectMocks
    private HousemateService housemateService;

    @Mock
    private AddedHousemateRepository addedHousemateRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                       .id(1L)
                       .email("test@example.com")
                       .name("TestUser")
                       .nickname("testUser")
                       .provider(Provider.GOOGLE)
                       .providerId("test123")
                       .status(Status.ONLINE)
                       .build();

        targetUser = User.builder()
                         .id(2L)
                         .email("target@example.com")
                         .name("Target")
                         .nickname("targetUser")
                         .provider(Provider.GOOGLE)
                         .providerId("target123")
                         .status(Status.OFFLINE)
                         .build();
    }

    @Test
    @DisplayName("이메일로 userId를 조회할 수 있다")
    void findUserIdByEmail() {
        // given
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // when
        Long userId = housemateService.findUserIdByUserId(testUser.getEmail());

        // then
        assertThat(userId).isEqualTo(testUser.getId());
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회시 예외가 발생한다")
    void findUserIdByEmail_NotFound() {
        // given
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> housemateService.findUserIdByUserId(nonExistentEmail))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("팔로잉 목록을 조회할 수 있다")
    void getFollowingList() {
        // given
        HousemateInfo housemateInfo = new HousemateInfo(
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getProfileImage(),
                targetUser.getBio(),
                targetUser.getStatus()
        );
        when(addedHousemateRepository.findByUserId(eq(testUser.getId()), any(), eq(21), any()))
                .thenReturn(Arrays.asList(housemateInfo));

        // when
        HousemateListResponse response = housemateService.getFollowingList(testUser.getId(), null, 20, null);

        // then
        assertThat(response.getHousemates()).hasSize(1);
        assertThat(response.getHousemates().get(0).getNickname()).isEqualTo(targetUser.getNickname());
        verify(addedHousemateRepository).findByUserId(eq(testUser.getId()), any(), eq(21), any());
    }

    @Test
    @DisplayName("팔로워 목록을 조회할 수 있다")
    void getFollowerList() {
        // given
        HousemateInfo housemateInfo = new HousemateInfo(
                targetUser.getId(),
                targetUser.getNickname(),
                targetUser.getProfileImage(),
                targetUser.getBio(),
                targetUser.getStatus()
        );
        when(addedHousemateRepository.findByAddedId(eq(testUser.getId()), any(), eq(21), any()))
                .thenReturn(Arrays.asList(housemateInfo));

        // when
        HousemateListResponse response = housemateService.getFollowerList(testUser.getId(), null, 20, null);

        // then
        assertThat(response.getHousemates()).hasSize(1);
        assertThat(response.getHousemates().get(0).getNickname()).isEqualTo(targetUser.getNickname());
        verify(addedHousemateRepository).findByAddedId(eq(testUser.getId()), any(), eq(21), any());
    }

    @Test
    @DisplayName("하우스메이트를 추가할 수 있다")
    void addHousemate() {
        // given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(addedHousemateRepository.existsByUserIdAndAddedId(testUser.getId(), targetUser.getId()))
                .thenReturn(false);

        // when
        housemateService.addHousemate(testUser.getId(), targetUser.getId());

        // then
        verify(addedHousemateRepository).save(any(AddedHousemate.class));
    }

    @Test
    @DisplayName("자기 자신을 하우스메이트로 추가할 수 없다")
    void addHousemate_SelfFollow() {
        // given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> housemateService.addHousemate(testUser.getId(), testUser.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이미 하우스메이트인 사용자를 다시 추가할 수 없다")
    void addHousemate_AlreadyExists() {
        // given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(addedHousemateRepository.existsByUserIdAndAddedId(testUser.getId(), targetUser.getId()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> housemateService.addHousemate(testUser.getId(), targetUser.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("하우스메이트를 삭제할 수 있다")
    void removeHousemate() {
        // given
        when(addedHousemateRepository.existsByUserIdAndAddedId(testUser.getId(), targetUser.getId()))
                .thenReturn(true);

        // when
        housemateService.removeHousemate(testUser.getId(), targetUser.getId());

        // then
        verify(addedHousemateRepository).deleteByUserIdAndAddedId(testUser.getId(), targetUser.getId());
    }

    @Test
    @DisplayName("하우스메이트 관계가 없는 경우 삭제할 수 없다")
    void removeHousemate_NotExists() {
        // given
        when(addedHousemateRepository.existsByUserIdAndAddedId(testUser.getId(), targetUser.getId()))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> housemateService.removeHousemate(testUser.getId(), targetUser.getId()))
                .isInstanceOf(BusinessException.class);
    }
}