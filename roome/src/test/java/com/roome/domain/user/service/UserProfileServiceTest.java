package com.roome.domain.user.service;

import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.entity.BookGenre;
import com.roome.domain.user.entity.MusicGenre;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGenreService userGenreService;

    private User testUser;
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                       .id(USER_ID)
                       .nickname("테스트")
                       .profileImage("test.jpg")
                       .bio("안녕하세요")
                       .build();
    }

    @Nested
    @DisplayName("getUserProfile 메서드는")
    class GetUserProfile {

        private final List<MusicGenre> musicGenres = Arrays.asList(MusicGenre.AFRO, MusicGenre.HIPHOP);
        private final List<BookGenre> bookGenres = Arrays.asList(BookGenre.SF, BookGenre.MYSTERY);

        @Test
        @DisplayName("자신의 프로필을 조회할 때 isOwner가 true로 반환된다")
        void whenViewingOwnProfile_returnsProfileWithIsOwnerTrue() {
            // given
            given(userRepository.getById(USER_ID)).willReturn(testUser);
            given(userGenreService.getUserMusicGenres(USER_ID)).willReturn(musicGenres);
            given(userGenreService.getUserBookGenres(USER_ID)).willReturn(bookGenres);

            // when
            UserProfileResponse response = userProfileService.getUserProfile(USER_ID, USER_ID);

            // then
            assertThat(response.isMyProfile()).isTrue();
            assertThat(response.getMusicGenres()).isEqualTo(musicGenres);
            assertThat(response.getBookGenres()).isEqualTo(bookGenres);
        }

        @Test
        @DisplayName("다른 사용자의 프로필을 조회할 때 isOwner가 false로 반환된다")
        void whenViewingOtherProfile_returnsProfileWithIsOwnerFalse() {
            // given
            Long otherUserId = 2L;
            given(userRepository.getById(USER_ID)).willReturn(testUser);
            given(userGenreService.getUserMusicGenres(USER_ID)).willReturn(musicGenres);
            given(userGenreService.getUserBookGenres(USER_ID)).willReturn(bookGenres);

            // when
            UserProfileResponse response = userProfileService.getUserProfile(USER_ID, otherUserId);

            // then
            assertThat(response.isMyProfile()).isFalse();
        }
    }

    @Nested
    @DisplayName("updateProfile 메서드는")
    class UpdateProfile {

        @Test
        @DisplayName("올바른 요청으로 프로필이 정상적으로 업데이트된다")
        void withValidRequest_updatesProfileSuccessfully() {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("새닉네임", "새소개");
            when(userRepository.getById(USER_ID)).thenReturn(testUser);
            when(userGenreService.getUserMusicGenres(USER_ID))
                    .thenReturn(Arrays.asList(MusicGenre.AFRO, MusicGenre.HIPHOP));
            when(userGenreService.getUserBookGenres(USER_ID))
                    .thenReturn(Arrays.asList(BookGenre.SF, BookGenre.MYSTERY));

            // when
            UserProfileResponse response = userProfileService.updateProfile(USER_ID, request);

            // then
            assertThat(response.getNickname()).isEqualTo("새닉네임");
            assertThat(response.getBio()).isEqualTo("새소개");
            verify(userRepository, times(2)).getById(USER_ID); // updateProfile과 getUserProfile에서 각각 한 번씩 호출
            verify(userGenreService).getUserMusicGenres(USER_ID);
            verify(userGenreService).getUserBookGenres(USER_ID);
        }

        @Test
        @DisplayName("잘못된 닉네임 형식으로 요청 시 예외가 발생한다")
        void withInvalidNickname_throwsException() {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("!", "새소개");
            given(userRepository.getById(USER_ID)).willReturn(testUser);

            // when & then
            assertThatThrownBy(() -> userProfileService.updateProfile(USER_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_NICKNAME_FORMAT);
        }

        @Test
        @DisplayName("자기소개가 30자를 초과하면 예외가 발생한다")
        void withTooLongBio_throwsException() {
            // given
            String tooLongBio = "이자기소개는삼십자를초과하는매우긴자기소개입니다제한을초과했습니다";
            UpdateProfileRequest request = new UpdateProfileRequest("새닉네임", tooLongBio);
            given(userRepository.getById(USER_ID)).willReturn(testUser);

            // when & then
            assertThatThrownBy(() -> userProfileService.updateProfile(USER_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_BIO_LENGTH);
        }
    }
}