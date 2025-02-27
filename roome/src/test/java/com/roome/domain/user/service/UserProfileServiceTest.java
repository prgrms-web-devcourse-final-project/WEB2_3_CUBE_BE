package com.roome.domain.user.service;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.BookGenre;
import com.roome.domain.book.entity.Genre;
import com.roome.domain.cd.entity.Cd;
import com.roome.domain.cd.entity.CdGenre;
import com.roome.domain.cd.entity.CdGenreType;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mycd.entity.MyCd;
import com.roome.domain.mycd.repository.MyCdRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MyCdRepository myCdRepository;

    @Mock
    private MyBookRepository myBookRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private User anotherUser;
    private User similarUser1;
    private User similarUser2;
    private Room testRoom;
    private List<MyCd> testUserCds;
    private List<MyBook> testUserBooks;
    private List<MyCd> similarUser1Cds;
    private List<MyBook> similarUser1Books;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 설정
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .nickname("TestUser")
                .profileImage("test.jpg")
                .bio("테스트 사용자입니다.")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("test123")
                .build();

        // 다른 사용자 설정
        anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .name("Another User")
                .nickname("AnotherUser")
                .profileImage("another.jpg")
                .bio("다른 테스트 사용자입니다.")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("another123")
                .build();

        similarUser1 = User.builder()
                .id(3L)
                .email("similar1@example.com")
                .name("Similar User 1")
                .nickname("similar1Nick")
                .profileImage("similar1-profile.jpg")
                .bio("Similar user 1 bio")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("similar123")
                .build();

        similarUser2 = User.builder()
                .id(4L)
                .email("similar2@example.com")
                .name("Similar User 2")
                .nickname("similar2Nick")
                .profileImage("similar2-profile.jpg")
                .bio("Similar user 2 bio")
                .status(Status.ONLINE)
                .provider(Provider.GOOGLE)
                .providerId("similar456")
                .build();

        // Room 설정
        testRoom = Room.builder()
                .id(1L)
                .user(testUser)
                .theme(RoomTheme.BASIC)
                .build();

        // CD 장르 타입 설정
        CdGenreType rockGenre = CdGenreType.builder()
                .id(1L)
                .name("Rock")
                .build();

        CdGenreType popGenre = CdGenreType.builder()
                .id(2L)
                .name("Pop")
                .build();

        CdGenreType jazzGenre = CdGenreType.builder()
                .id(3L)
                .name("Jazz")
                .build();

        // CD 설정
        Cd rockCd = Cd.builder()
                .id(1L)
                .title("Rock Album")
                .artist("Rock Artist")
                .album("Rock Album")
                .releaseDate(LocalDate.now())
                .coverUrl("rock-cover.jpg")
                .youtubeUrl("youtube.com/rock")
                .duration(180)
                .cdGenres(new ArrayList<>())
                .build();

        CdGenre rockCdGenre = CdGenre.builder()
                .id(1L)
                .cd(rockCd)
                .genreType(rockGenre)
                .build();

        rockCd.addGenre(rockCdGenre);

        Cd popCd = Cd.builder()
                .id(2L)
                .title("Pop Album")
                .artist("Pop Artist")
                .album("Pop Album")
                .releaseDate(LocalDate.now())
                .coverUrl("pop-cover.jpg")
                .youtubeUrl("youtube.com/pop")
                .duration(200)
                .cdGenres(new ArrayList<>())
                .build();

        CdGenre popCdGenre = CdGenre.builder()
                .id(2L)
                .cd(popCd)
                .genreType(popGenre)
                .build();

        popCd.addGenre(popCdGenre);

        Cd jazzCd = Cd.builder()
                .id(3L)
                .title("Jazz Album")
                .artist("Jazz Artist")
                .album("Jazz Album")
                .releaseDate(LocalDate.now())
                .coverUrl("jazz-cover.jpg")
                .youtubeUrl("youtube.com/jazz")
                .duration(240)
                .cdGenres(new ArrayList<>())
                .build();

        CdGenre jazzCdGenre = CdGenre.builder()
                .id(3L)
                .cd(jazzCd)
                .genreType(jazzGenre)
                .build();

        jazzCd.addGenre(jazzCdGenre);

        // MyCd 생성
        testUserCds = new ArrayList<>();
        MyCd myCd1 = MyCd.create(testUser, testRoom, rockCd);
        MyCd myCd2 = MyCd.create(testUser, testRoom, popCd);
        testUserCds.add(myCd1);
        testUserCds.add(myCd2);

        // Similar 사용자의 CD 컬렉션
        similarUser1Cds = new ArrayList<>();
        MyCd similarCd1 = MyCd.create(similarUser1, testRoom, rockCd);
        MyCd similarCd2 = MyCd.create(similarUser1, testRoom, jazzCd);
        similarUser1Cds.add(similarCd1);
        similarUser1Cds.add(similarCd2);

        // 책 장르 설정
        Genre fictionGenre = Genre.builder()
                .id(1L)
                .name("Fiction")
                .build();

        Genre scienceGenre = Genre.builder()
                .id(2L)
                .name("Science")
                .build();

        Genre romanceGenre = Genre.builder()
                .id(3L)
                .name("Romance")
                .build();

        // 책 설정
        Book fictionBook = Book.builder()
                .id(1L)
                .title("Fiction Book")
                .author("Fiction Author")
                .isbn("9781234567890")
                .publisher("Fiction Publisher")
                .imageUrl("fiction.jpg")
                .publishedDate(LocalDate.now())
                .page(300L)
                .bookGenres(new ArrayList<>())
                .build();

        BookGenre fictionBookGenre = BookGenre.builder()
                .id(1L)
                .book(fictionBook)
                .genre(fictionGenre)
                .build();

        fictionBook.addBookGenre(fictionBookGenre);

        Book scienceBook = Book.builder()
                .id(2L)
                .title("Science Book")
                .author("Science Author")
                .isbn("9789876543210")
                .publisher("Science Publisher")
                .imageUrl("science.jpg")
                .publishedDate(LocalDate.now())
                .page(250L)
                .bookGenres(new ArrayList<>())
                .build();

        BookGenre scienceBookGenre = BookGenre.builder()
                .id(2L)
                .book(scienceBook)
                .genre(scienceGenre)
                .build();

        scienceBook.addBookGenre(scienceBookGenre);

        Book romanceBook = Book.builder()
                .id(3L)
                .title("Romance Book")
                .author("Romance Author")
                .isbn("9783456789012")
                .publisher("Romance Publisher")
                .imageUrl("romance.jpg")
                .publishedDate(LocalDate.now())
                .page(200L)
                .bookGenres(new ArrayList<>())
                .build();

        BookGenre romanceBookGenre = BookGenre.builder()
                .id(3L)
                .book(romanceBook)
                .genre(romanceGenre)
                .build();

        romanceBook.addBookGenre(romanceBookGenre);

        // MyBook 생성
        testUserBooks = new ArrayList<>();
        MyBook myBook1 = MyBook.create(testUser, testRoom, fictionBook);
        MyBook myBook2 = MyBook.create(testUser, testRoom, scienceBook);
        testUserBooks.add(myBook1);
        testUserBooks.add(myBook2);

        // Similar 사용자의 책 컬렉션
        similarUser1Books = new ArrayList<>();
        MyBook similarBook1 = MyBook.create(similarUser1, testRoom, fictionBook);
        MyBook similarBook2 = MyBook.create(similarUser1, testRoom, romanceBook);
        similarUser1Books.add(similarBook1);
        similarUser1Books.add(similarBook2);
    }

    @Test
    @DisplayName("사용자 프로필 조회 성공")
    void getUserProfile_Success() {
        // given
        Long targetUserId = 1L;
        Long currentUserId = 1L;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(myCdRepository.findByUserId(targetUserId)).thenReturn(testUserCds);
        when(myBookRepository.findAllByUserId(targetUserId)).thenReturn(testUserBooks);
        when(userRepository.findByIdNot(targetUserId)).thenReturn(Collections.emptyList());

        // when
        UserProfileResponse response = userProfileService.getUserProfile(targetUserId, currentUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getNickname()).isEqualTo("TestUser");
        assertThat(response.getProfileImage()).isEqualTo("test.jpg");
        assertThat(response.getBio()).isEqualTo("테스트 사용자입니다.");
        assertThat(response.isMyProfile()).isTrue();

        // 장르 검증
        assertThat(response.getMusicGenres()).hasSize(2);
        assertThat(response.getMusicGenres()).contains("Rock", "Pop");
        assertThat(response.getBookGenres()).hasSize(2);
        assertThat(response.getBookGenres()).contains("Fiction", "Science");

        // 추천 사용자는 없어야 함 (유사 장르가 없으므로)
        assertThat(response.getRecommendedUsers()).isEmpty();

        // 검증: getById가 호출되었는지
        verify(userRepository, times(1)).findById(targetUserId);
    }

    @Test
    @DisplayName("다른 사용자의 프로필 조회 성공")
    void getOtherUserProfile_Success() {
        // given
        Long targetUserId = 2L;
        Long currentUserId = 1L;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(anotherUser));
        when(myCdRepository.findByUserId(targetUserId)).thenReturn(Collections.emptyList());
        when(myBookRepository.findAllByUserId(targetUserId)).thenReturn(Collections.emptyList());

        // when
        UserProfileResponse response = userProfileService.getUserProfile(targetUserId, currentUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("2");
        assertThat(response.getNickname()).isEqualTo("AnotherUser");
        assertThat(response.getProfileImage()).isEqualTo("another.jpg");
        assertThat(response.getBio()).isEqualTo("다른 테스트 사용자입니다.");
        assertThat(response.isMyProfile()).isFalse();
        assertThat(response.getMusicGenres()).isEmpty();
        assertThat(response.getBookGenres()).isEmpty();
    }

    @Test
    @DisplayName("유사한 취향의 사용자 추천 테스트")
    void recommendSimilarUsers_Success() {
        // given
        Long targetUserId = 1L;
        Long currentUserId = 1L;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(myCdRepository.findByUserId(targetUserId)).thenReturn(testUserCds);
        when(myBookRepository.findAllByUserId(targetUserId)).thenReturn(testUserBooks);
        when(userRepository.findByIdNot(targetUserId)).thenReturn(List.of(similarUser1, similarUser2));

        // similar1 사용자 설정 - Rock CD와 Fiction 책을 가지고 있음
        when(myCdRepository.findByUserId(similarUser1.getId())).thenReturn(similarUser1Cds);
        when(myBookRepository.findAllByUserId(similarUser1.getId())).thenReturn(similarUser1Books);

        // similar2 사용자 설정 - 아무것도 없음
        when(myCdRepository.findByUserId(similarUser2.getId())).thenReturn(Collections.emptyList());
        when(myBookRepository.findAllByUserId(similarUser2.getId())).thenReturn(Collections.emptyList());

        // when
        UserProfileResponse response = userProfileService.getUserProfile(targetUserId, currentUserId);

        // then
        assertThat(response.getRecommendedUsers()).hasSize(1);
        assertThat(response.getRecommendedUsers().get(0).getUserId()).isEqualTo(similarUser1.getId());
        assertThat(response.getRecommendedUsers().get(0).getNickname()).isEqualTo(similarUser1.getNickname());
    }

    @Test
    @DisplayName("프로필 업데이트 성공")
    void updateProfile_Success() {
        // given
        Long userId = 1L;
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("UpdatedUser")
                .bio("업데이트된 자기소개입니다.")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // 모든 호출을 모킹
        UserProfileResponse mockResponse = UserProfileResponse.builder()
                .id("1")
                .nickname("UpdatedUser")
                .profileImage("test.jpg")
                .bio("업데이트된 자기소개입니다.")
                .isMyProfile(true)
                .build();

        // getUserProfile 메서드 모킹 (updateProfile에서 내부적으로 호출하므로)
        UserProfileService spyService = spy(userProfileService);
        doReturn(mockResponse).when(spyService).getUserProfile(eq(userId), eq(userId));

        // when
        UserProfileResponse response = spyService.updateProfile(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo("UpdatedUser");
        assertThat(response.getBio()).isEqualTo("업데이트된 자기소개입니다.");

        // 검증: getById가 한 번만 호출되었는지
        verify(userRepository, times(1)).findById(userId);
        // 검증: getUserProfile이 한 번 호출되었는지
        verify(spyService, times(1)).getUserProfile(userId, userId);
    }

    @Test
    @DisplayName("CD 컬렉션이 없는 사용자 테스트")
    void getUserProfile_NoCdCollection_Success() {
        // given
        Long targetUserId = 1L;
        Long currentUserId = 1L;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(myCdRepository.findByUserId(targetUserId)).thenReturn(Collections.emptyList());
        when(myBookRepository.findAllByUserId(targetUserId)).thenReturn(testUserBooks);
        when(userRepository.findByIdNot(targetUserId)).thenReturn(Collections.emptyList());

        // when
        UserProfileResponse response = userProfileService.getUserProfile(targetUserId, currentUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMusicGenres()).isEmpty();
        assertThat(response.getBookGenres()).hasSize(2);
    }

    @Test
    @DisplayName("책 컬렉션이 없는 사용자 테스트")
    void getUserProfile_NoBookCollection_Success() {
        // given
        Long targetUserId = 1L;
        Long currentUserId = 1L;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(myCdRepository.findByUserId(targetUserId)).thenReturn(testUserCds);
        when(myBookRepository.findAllByUserId(targetUserId)).thenReturn(Collections.emptyList());
        when(userRepository.findByIdNot(targetUserId)).thenReturn(Collections.emptyList());

        // when
        UserProfileResponse response = userProfileService.getUserProfile(targetUserId, currentUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMusicGenres()).hasSize(2);
        assertThat(response.getBookGenres()).isEmpty();
    }
}