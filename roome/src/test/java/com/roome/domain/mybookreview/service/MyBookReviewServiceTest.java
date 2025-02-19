package com.roome.domain.mybookreview.service;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.repository.BookRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybook.exception.DoNotHavePermissionToMyBookException;
import com.roome.domain.mybookreview.entity.MyBookReview;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mybookreview.exception.MyBookAuthorizationException;
import com.roome.domain.mybookreview.exception.MyBookReviewNotFoundException;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class MyBookReviewServiceTest {

    @Autowired
    private MyBookReviewService myBookReviewService;

    @Autowired
    private MyBookReviewRepository myBookReviewRepository;

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookRepository bookRepository;

    @DisplayName("등록 도서에 대한 서평을 작성할 수 있다.")
    @Test
    void create() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user);
        myBookRepository.save(myBook);

        MyBookReviewCreateRequest request = new MyBookReviewCreateRequest(
                "title",
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                "BLUE"
        );

        // when
        MyBookReviewResponse response = myBookReviewService.create(user.getId(), myBook.getId(), request);

        // then
        assertThat(response.title()).isEqualTo(request.title());
    }

    @DisplayName("도서를 등록한 사용자가 아닌 사용자가 서평을 작성하려는 경우 예외가 발생한다.")
    @Test
    void createWhoIsNotOwnerOfBook() {

        // given
        User user1 = createUser("user1@gmail.com", "user1", "provId1");
        User user2 = createUser("user2@gmail.com", "user2", "provId2");
        userRepository.saveAll(List.of(user1, user2));

        Room room = createRoom(user1);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user1);
        myBookRepository.save(myBook);

        MyBookReviewCreateRequest request = new MyBookReviewCreateRequest(
                "title",
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                "BLUE"
        );

        // when // then
        assertThatThrownBy(() -> myBookReviewService.create(user2.getId(), myBook.getId(), request))
                .isInstanceOf(DoNotHavePermissionToMyBookException.class)
                .hasMessage("등록 도서에 대한 권한이 없는 사용자입니다.");
    }

    @DisplayName("등록 도서에 작성한 서평을 조회할 수 있다.")
    @Test
    void read() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user);
        myBookRepository.save(myBook);

        MyBookReview review = createMyBookReview("review1", user, myBook);
        myBookReviewRepository.save(review);

        // when
        MyBookReviewResponse response = myBookReviewService.read(myBook.getId());

        // then
        assertThat(response.title()).isEqualTo(review.getTitle());
    }

    @DisplayName("등록 도서에 작성한 서평이 없는 경우 조회하면 예외가 발생한다.")
    @Test
    void readReviewIsEmpty() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user);
        myBookRepository.save(myBook);

        // when // then
        assertThatThrownBy(() -> myBookReviewService.read(myBook.getId()))
                .isInstanceOf(MyBookReviewNotFoundException.class)
                .hasMessage("서평을 찾을 수 없습니다.");
    }

    @DisplayName("등록 도서에 작성한 서평을 수정할 수 있다.")
    @Test
    void update() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user);
        myBookRepository.save(myBook);

        MyBookReview review = createMyBookReview("title", user, myBook);
        myBookReviewRepository.save(review);

        MyBookReviewUpdateRequest request = new MyBookReviewUpdateRequest(
                "new-title",
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                "BLUE"
        );

        // when
        MyBookReviewResponse response = myBookReviewService.update(user.getId(), review.getId(), request);

        // then
        assertThat(response.title()).isEqualTo(request.title());
    }

    @DisplayName("서평을 작성한 사용자가 아닌 사용자가 서평을 수정하는 경우 예외가 발생한다.")
    @Test
    void updateWhoIsNotOwnerOfBook() {

        // given
        User user1 = createUser("user1@gmail.com", "user1", "provId1");
        User user2 = createUser("user2@gmail.com", "user2", "provId2");
        userRepository.saveAll(List.of(user1, user2));

        Room room = createRoom(user1);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user1);
        myBookRepository.save(myBook);

        MyBookReview review = createMyBookReview("title", user1, myBook);
        myBookReviewRepository.save(review);

        MyBookReviewUpdateRequest request = new MyBookReviewUpdateRequest(
                "new-title",
                "quote",
                "takeaway",
                "motivate",
                "topic",
                "freeFormText",
                "BLUE"
        );

        // when // then
        assertThatThrownBy(() -> myBookReviewService.update(user2.getId(), review.getId(), request))
                .isInstanceOf(MyBookAuthorizationException.class)
                .hasMessage("서평에 대한 권한이 없는 사용자입니다.");
    }

    @DisplayName("서평을 삭제할 수 있다.")
    @Test
    void delete() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user);
        myBookRepository.save(myBook);

        MyBookReview review = createMyBookReview("title", user, myBook);
        myBookReviewRepository.save(review);

        // when
        myBookReviewService.delete(user.getId(), review.getId());

        // then
        boolean deleted = myBookReviewRepository.findById(review.getId()).isEmpty();
        assertThat(deleted).isTrue();
    }

    @DisplayName("서평을 작성한 사용자가 아닌 사용자가 서평을 삭제하는 경우 예외가 발생한다.")
    @Test
    void deleteWhoIsNotOwnerOfBook() {

        // given
        User user1 = createUser("user1@gmail.com", "user1", "provId1");
        User user2 = createUser("user2@gmail.com", "user2", "provId2");
        userRepository.saveAll(List.of(user1, user2));

        Room room = createRoom(user1);
        roomRepository.save(room);

        Book book = createBook(1L, "book1");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user1);
        myBookRepository.save(myBook);

        MyBookReview review = createMyBookReview("title", user1, myBook);
        myBookReviewRepository.save(review);

        // when // then
        assertThatThrownBy(() -> myBookReviewService.delete(user2.getId(), review.getId()))
                .isInstanceOf(MyBookAuthorizationException.class)
                .hasMessage("서평에 대한 권한이 없는 사용자입니다.");
    }

    private User createUser(String email, String name, String providerId) {
        return User.builder()
                .email(email)
                .name(name)
                .nickname("nickname")
                .profileImage("profile")
                .provider(Provider.GOOGLE)
                .providerId(providerId)
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

    private MyBookReview createMyBookReview(String title, User user, MyBook myBook) {
        return MyBookReview.builder()
                .title(title)
                .quote("quote")
                .takeaway("takeaway")
                .motivate("motivate")
                .topic("topic")
                .freeFormText("freeFormText")
                .user(user)
                .myBook(myBook)
                .build();
    }
}