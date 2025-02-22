package com.roome.domain.user.dto.response;

import com.roome.domain.user.entity.BookGenre;
import com.roome.domain.user.entity.MusicGenre;
import com.roome.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserProfileResponse {
    private String id;
    private String nickname;
    private String profileImage;
    private String bio;
    private List<MusicGenre> musicGenres;
    private List<BookGenre> bookGenres;
    private boolean isMyProfile;

    public static UserProfileResponse of(User user, List<MusicGenre> musicGenres,
                                         List<BookGenre> bookGenres, boolean isMyProfile) {
        return UserProfileResponse.builder()
                                  .id(user.getId().toString())
                                  .nickname(user.getNickname())
                                  .profileImage(user.getProfileImage())
                                  .bio(user.getBio())
                                  .musicGenres(musicGenres)
                                  .bookGenres(bookGenres)
                                  .isMyProfile(isMyProfile)
                                  .build();
    }
}
