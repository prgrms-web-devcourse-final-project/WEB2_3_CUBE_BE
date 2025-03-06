package com.roome.domain.userGenrePreference.service;

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
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.userGenrePreference.entity.GenreType;
import com.roome.domain.userGenrePreference.entity.UserGenrePreference;
import com.roome.domain.userGenrePreference.repository.UserGenrePreferenceRepository;
import com.roome.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenrePreferenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MyCdRepository myCdRepository;

    @Mock
    private MyBookRepository myBookRepository;

    @Mock
    private UserGenrePreferenceRepository userGenrePreferenceRepository;

    @InjectMocks
    private GenrePreferenceService genrePreferenceService;

    private User testUser;
    private MyCd testMyCd1, testMyCd2, testMyCd3;
    private MyBook testMyBook1, testMyBook2, testMyBook3;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .nickname("testuser")
                .build();

        // CD 설정
        Cd cd1 = createCd(1L, "Album 1", "Artist 1", List.of("Pop", "Rock"));
        Cd cd2 = createCd(2L, "Album 2", "Artist 2", List.of("Rock", "Metal"));
        Cd cd3 = createCd(3L, "Album 3", "Artist 3", List.of("Pop", "Jazz"));

        Room room = Room.builder().id(1L).user(testUser).build();

        testMyCd1 = MyCd.create(testUser, room, cd1);
        testMyCd2 = MyCd.create(testUser, room, cd2);
        testMyCd3 = MyCd.create(testUser, room, cd3);

        // Book 설정
        Book book1 = createBook(1L, "Book 1", "Author 1", List.of("Fiction", "Fantasy"));
        Book book2 = createBook(2L, "Book 2", "Author 2", List.of("Fiction", "Sci-Fi"));
        Book book3 = createBook(3L, "Book 3", "Author 3", List.of("Non-Fiction", "Biography"));

        testMyBook1 = MyBook.create(testUser, room, book1);
        testMyBook2 = MyBook.create(testUser, room, book2);
        testMyBook3 = MyBook.create(testUser, room, book3);
    }

    @Test
    @DisplayName("CD 장르 선호도 조회 성공")
    void getTopCdGenres_ShouldReturnGenreList() {
        // Given
        List<UserGenrePreference> preferences = Arrays.asList(
                UserGenrePreference.create(testUser, GenreType.CD, "Rock", 2, 1),
                UserGenrePreference.create(testUser, GenreType.CD, "Pop", 2, 2),
                UserGenrePreference.create(testUser, GenreType.CD, "Jazz", 1, 3)
        );

        when(userGenrePreferenceRepository.findByUserIdAndGenreTypeOrderByRankAsc(
                eq(1L), eq(GenreType.CD))).thenReturn(preferences);

        // When
        List<String> result = genrePreferenceService.getTopCdGenres(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Rock", "Pop", "Jazz");
        verify(userGenrePreferenceRepository).findByUserIdAndGenreTypeOrderByRankAsc(
                eq(1L), eq(GenreType.CD));
    }

    @Test
    @DisplayName("책 장르 선호도 조회 성공")
    void getTopBookGenres_ShouldReturnGenreList() {
        // Given
        List<UserGenrePreference> preferences = Arrays.asList(
                UserGenrePreference.create(testUser, GenreType.BOOK, "Fiction", 2, 1),
                UserGenrePreference.create(testUser, GenreType.BOOK, "Fantasy", 1, 2),
                UserGenrePreference.create(testUser, GenreType.BOOK, "Sci-Fi", 1, 3)
        );

        when(userGenrePreferenceRepository.findByUserIdAndGenreTypeOrderByRankAsc(
                eq(1L), eq(GenreType.BOOK))).thenReturn(preferences);

        // When
        List<String> result = genrePreferenceService.getTopBookGenres(1L);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Fiction", "Fantasy", "Sci-Fi");
        verify(userGenrePreferenceRepository).findByUserIdAndGenreTypeOrderByRankAsc(
                eq(1L), eq(GenreType.BOOK));
    }

    @Test
    @DisplayName("CD 장르 선호도 업데이트 성공 - CD가 있는 경우")
    void updateCdGenrePreferences_WithCds_ShouldSavePreferences() {
        // Given
        Long userId = 1L;
        List<MyCd> myCds = Arrays.asList(testMyCd1, testMyCd2, testMyCd3);

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
        when(myCdRepository.findByUserId(eq(userId))).thenReturn(myCds);

        // When
        genrePreferenceService.updateCdGenrePreferences(userId);

        // Then
        verify(userRepository).findById(eq(userId));
        verify(myCdRepository).findByUserId(eq(userId));
        verify(userGenrePreferenceRepository).deleteByUserIdAndGenreType(eq(userId), eq(GenreType.CD));
        verify(userGenrePreferenceRepository, times(3)).save(any(UserGenrePreference.class));
    }

    @Test
    @DisplayName("CD 장르 선호도 업데이트 - CD가 없는 경우")
    void updateCdGenrePreferences_WithoutCds_ShouldDeleteExistingPreferences() {
        // Given
        Long userId = 1L;
        List<MyCd> myCds = Collections.emptyList();

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
        when(myCdRepository.findByUserId(eq(userId))).thenReturn(myCds);

        // When
        genrePreferenceService.updateCdGenrePreferences(userId);

        // Then
        verify(userRepository).findById(eq(userId));
        verify(myCdRepository).findByUserId(eq(userId));
        verify(userGenrePreferenceRepository).deleteByUserIdAndGenreType(eq(userId), eq(GenreType.CD));
        verify(userGenrePreferenceRepository, never()).save(any(UserGenrePreference.class));
    }

    @Test
    @DisplayName("책 장르 선호도 업데이트 성공 - 책이 있는 경우")
    void updateBookGenrePreferences_WithBooks_ShouldSavePreferences() {
        // Given
        Long userId = 1L;
        List<MyBook> myBooks = Arrays.asList(testMyBook1, testMyBook2, testMyBook3);

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
        when(myBookRepository.findAllByUserId(eq(userId))).thenReturn(myBooks);

        // When
        genrePreferenceService.updateBookGenrePreferences(userId);

        // Then
        verify(userRepository).findById(eq(userId));
        verify(myBookRepository).findAllByUserId(eq(userId));
        verify(userGenrePreferenceRepository).deleteByUserIdAndGenreType(eq(userId), eq(GenreType.BOOK));
        verify(userGenrePreferenceRepository, times(3)).save(any(UserGenrePreference.class));
    }

    @Test
    @DisplayName("책 장르 선호도 업데이트 - 책이 없는 경우")
    void updateBookGenrePreferences_WithoutBooks_ShouldDeleteExistingPreferences() {
        // Given
        Long userId = 1L;
        List<MyBook> myBooks = Collections.emptyList();

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
        when(myBookRepository.findAllByUserId(eq(userId))).thenReturn(myBooks);

        // When
        genrePreferenceService.updateBookGenrePreferences(userId);

        // Then
        verify(userRepository).findById(eq(userId));
        verify(myBookRepository).findAllByUserId(eq(userId));
        verify(userGenrePreferenceRepository).deleteByUserIdAndGenreType(eq(userId), eq(GenreType.BOOK));
        verify(userGenrePreferenceRepository, never()).save(any(UserGenrePreference.class));
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 때 예외 발생")
    void updateCdGenrePreferences_WithNonExistingUser_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            genrePreferenceService.updateCdGenrePreferences(userId);
        });

        verify(userRepository).findById(eq(userId));
        verify(myCdRepository, never()).findByUserId(any());
    }

    // Helper methods
    private Cd createCd(Long id, String title, String artist, List<String> genreNames) {
        Cd cd = Cd.builder()
                .id(id)
                .title(title)
                .artist(artist)
                .build();

        List<CdGenre> cdGenres = new ArrayList<>();
        for (String genreName : genreNames) {
            // CdGenreType 객체 생성 및 설정
            CdGenreType genreType = CdGenreType.builder()
                    .id((long) genreNames.indexOf(genreName) + 1)
                    .name(genreName)
                    .build();

            // CdGenre 객체 생성 및 연결
            CdGenre cdGenre = CdGenre.builder()
                    .cd(cd)
                    .genreType(genreType)
                    .build();

            cdGenres.add(cdGenre);
        }

        // CD에 장르 추가
        for (CdGenre cdGenre : cdGenres) {
            cd.addGenre(cdGenre);
        }
        return cd;
    }

    private Book createBook(Long id, String title, String author, List<String> genreNames) {
        Book book = Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .build();

        List<BookGenre> bookGenres = new ArrayList<>();
        for (String genreName : genreNames) {
            // Genre 객체 생성 (Genre.create 메소드 사용)
            // 실제 서비스에서는 ID가 자동 생성되므로 여기서는 설정하지 않음
            Genre genre = Genre.create(genreName);

            // BookGenre 객체 생성 및 연결
            BookGenre bookGenre = BookGenre.builder()
                    .book(book)
                    .genre(genre)
                    .build();
            bookGenres.add(bookGenre);
        }

        // Book에 장르 추가
        for (BookGenre bookGenre : bookGenres) {
            book.addBookGenre(bookGenre);
        }
        return book;
    }
}