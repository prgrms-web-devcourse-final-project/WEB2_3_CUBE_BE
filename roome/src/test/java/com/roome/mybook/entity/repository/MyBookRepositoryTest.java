package com.roome.mybook.entity.repository;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.repository.BookRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.Provider;
import com.roome.domain.user.entity.Status;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class MyBookRepositoryTest {

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("사용자가 등록한 도서 목록을 최신 등록일 순으로 페이징 해서 조회할 수 있다. 첫 번째 페이지 조회 쿼리.")
    @Test
    void findAllFirstPage() {

        // given
        User user = createUser("user@gmail.com", "user");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book1 = createBook(1L, "book1");
        Book book2 = createBook(2L, "book2");
        Book book3 = createBook(3L, "book3");
        Book book4 = createBook(4L, "book4");
        Book book5 = createBook(5L, "book5");
        bookRepository.saveAll(List.of(book1, book2, book3, book4, book5));

        MyBook myBook1 = createMyBook(room, book1, user);
        MyBook myBook2 = createMyBook(room, book2, user);
        MyBook myBook3 = createMyBook(room, book3, user);
        MyBook myBook4 = createMyBook(room, book4, user);
        MyBook myBook5 = createMyBook(room, book5, user);
        myBookRepository.saveAll(List.of(myBook1, myBook2, myBook3, myBook4, myBook5));

        Long limit = 3L;

        // when
        List<MyBook> myBooks = myBookRepository.findAll(room.getId(), limit);

        // then
        assertThat(myBooks).hasSize(3)
                .extracting("book.title")
                .containsExactly(book5.getTitle(), book4.getTitle(), book3.getTitle());
    }

    @DisplayName("사용자가 등록한 도서 목록을 최신 등록일 순으로 페이징 해서 조회할 수 있다. 두 번째 ~ 마지막 페이지 조회 쿼리.")
    @Test
    void findAllAfterFirstPage() {

        // given
        User user = createUser("user@gmail.com", "user");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book1 = createBook(1L, "book1");
        Book book2 = createBook(2L, "book2");
        Book book3 = createBook(3L, "book3");
        Book book4 = createBook(4L, "book4");
        Book book5 = createBook(5L, "book5");
        bookRepository.saveAll(List.of(book1, book2, book3, book4, book5));

        MyBook myBook1 = createMyBook(room, book1, user);
        MyBook myBook2 = createMyBook(room, book2, user);
        MyBook myBook3 = createMyBook(room, book3, user);
        MyBook myBook4 = createMyBook(room, book4, user);
        MyBook myBook5 = createMyBook(room, book5, user);
        myBookRepository.saveAll(List.of(myBook1, myBook2, myBook3, myBook4, myBook5));

        Long limit = 3L;
        Long lastMyBookId = myBook3.getId();

        // when
        List<MyBook> myBooks = myBookRepository.findAll(room.getId(), limit, lastMyBookId);

        // then
        assertThat(myBooks).hasSize(2)
                .extracting("book.title")
                .containsExactly(book2.getTitle(), book1.getTitle());
    }


    @DisplayName("사용자가 등록한 도서를 원하는 개수만큼 삭제할 수 있다.")
    @Test
    void deleteAllIn() {

        // given
        User user = createUser("user@gmail.com", "user");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book1 = createBook(1L, "book1");
        Book book2 = createBook(2L, "book2");
        Book book3 = createBook(3L, "book3");
        bookRepository.saveAll(List.of(book1, book2, book3));

        MyBook myBook1 = createMyBook(room, book1, user);
        MyBook myBook2 = createMyBook(room, book2, user);
        MyBook myBook3 = createMyBook(room, book3, user);
        myBookRepository.saveAll(List.of(myBook1, myBook2, myBook3));

        List<String> ids = List.of(
                String.valueOf(myBook1.getId()),
                String.valueOf(myBook2.getId())
        );

        // when
        myBookRepository.deleteAllIn(ids);

        // then
        List<MyBook> myBooks = myBookRepository.findAll();
        assertThat(myBooks).hasSize(1)
                .extracting("book.title")
                .contains(book3.getTitle());
    }

    private User createUser(String email, String name) {
        return User.builder()
                .email(email)
                .name(name)
                .nickname("nickname")
                .profileImage("profile")
                .provider(Provider.GOOGLE)
                .providerId("provId")
                .status(Status.ONLINE)
                .lastLogin(LocalDateTime.of(2025, 1, 1, 1, 1))
                .refreshToken("refToken")
                .build();
    }

    private Room createRoom(User user) {
        return Room.builder()
                .user(user)
                .theme(RoomTheme.BASIC)
                .createdAt(LocalDateTime.of(2025, 1, 1, 1, 1))
                .furnitures(null)
                .build();
    }

    private Book createBook(Long isbn, String title) {
        return Book.builder()
                .isbn(isbn)
                .title(title)
                .author("author1")
                .publisher("publisher1")
                .publishedDate(LocalDate.of(2025, 1, 1))
                .imageUrl("url")
                .category("category")
                .page(100L)
                .build();
    }

    private MyBook createMyBook(Room room, Book book, User user) {
        return MyBook.builder()
                .room(room)
                .book(book)
                .user(user)
                .build();
    }
}