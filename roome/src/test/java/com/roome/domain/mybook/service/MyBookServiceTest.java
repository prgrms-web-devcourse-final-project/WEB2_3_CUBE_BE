package com.roome.domain.mybook.service;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.repository.BookRepository;
import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.repository.FurnitureRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybook.exception.MyBookDuplicateException;
import com.roome.domain.mybook.exception.MyBookNotFoundException;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.entity.RoomTheme;
import com.roome.domain.room.exception.RoomAuthorizationException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class MyBookServiceTest {

    @Autowired
    private MyBookService myBookService;

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private MyBookCountRepository myBookCountRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FurnitureRepository furnitureRepository;

    @DisplayName("도서를 등록할 수 있다. 등록하고자 하는 도서가 테이블에 없는 경우.")
    @Test
    void createBookDoesNotExist() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        room = roomRepository.save(room);

        Furniture furniture = createFurniture(room);
        furnitureRepository.save(furniture);

        room.setFurnitures(List.of(furniture));

        List<String> genreNames = List.of("IT", "웹");
        MyBookCreateRequest request = new MyBookCreateRequest(
                1L,
                "title",
                "author",
                "publisher",
                LocalDate.of(2025, 1, 1),
                "image.jpg",
                genreNames,
                1231L
        );

        // when
        MyBookResponse myBookResponse = myBookService.create(user.getId(), user.getId(), request);

        // then
        MyBook myBook = myBookRepository.findById(myBookResponse.id()).orElseThrow();
        assertThat(myBook.getBook().getTitle()).isEqualTo(request.title());
        assertThat(myBook.getBook().getBookGenres()).hasSize(2)
                .extracting("genre.name")
                .containsExactlyInAnyOrder("IT", "웹");

        Long count = myBookCountRepository.findByUserId(user.getId())
                .map(MyBookCount::getCount)
                .orElse(0L);
        assertThat(count).isEqualTo(1L);
    }

    @DisplayName("도서를 등록할 수 있다. 등록하고자 하는 도서가 이미 테이블에 있는 경우.")
    @Test
    void createBookExist() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        room = roomRepository.save(room);

        Furniture furniture = createFurniture(room);
        furnitureRepository.save(furniture);

        room.setFurnitures(List.of(furniture));

        Book book = createBook(1L, "title");
        bookRepository.save(book);

        MyBookCreateRequest request = new MyBookCreateRequest(
                book.getIsbn(),
                book.getTitle(),
                "author",
                "publisher",
                LocalDate.of(2025, 1, 1),
                "image.jpg",
                List.of(),
                100L
        );

        // when
        MyBookResponse myBookResponse = myBookService.create(user.getId(), user.getId(), request);

        // then
        MyBook myBook = myBookRepository.findById(myBookResponse.id()).orElseThrow();
        assertThat(myBook.getBook().getTitle()).isEqualTo(request.title());

        Long count = myBookCountRepository.findByUserId(user.getId())
                .map(MyBookCount::getCount)
                .orElse(0L);
        assertThat(count).isEqualTo(1L);
    }

    @DisplayName("방의 주인이 아닌 사용자가 도서를 등록할 경우 예외가 발생한다.")
    @Test
    void createWhoIsNotOwnerOfRoom() {

        // given
        User user1 = createUser("user1@gmail.com", "user1", "provId1");
        User user2 = createUser("user2@gmail.com", "user2", "provId2");
        userRepository.saveAll(List.of(user1, user2));

        Room room = createRoom(user1);
        roomRepository.save(room);

        Book book = createBook(1L, "book");
        bookRepository.save(book);

        MyBookCreateRequest request = new MyBookCreateRequest(
                book.getIsbn(),
                book.getTitle(),
                book.getTitle(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getImageUrl(),
                List.of(),
                book.getPage()
        );

        // when // then
        assertThatThrownBy(() -> myBookService.create(user2.getId(), user1.getId(), request))
                .isInstanceOf(RoomAuthorizationException.class)
                .hasMessage("해당 방의 소유주가 아닙니다.");
    }

    @DisplayName("이미 등록된 도서를 등록하려고 하는 경우 예외가 발생한다.")
    @Test
    void createDuplicateMyBook() {

        // given
        User user1 = createUser("user1@gmail.com", "user1", "provId1");
        userRepository.save(user1);

        Room room = createRoom(user1);
        room = roomRepository.save(room);

        Furniture furniture = createFurniture(room);
        furnitureRepository.save(furniture);

        room.setFurnitures(List.of(furniture));

        Book book = createBook(1L, "book");
        bookRepository.save(book);

        MyBook myBook = createMyBook(room, book, user1);
        myBookRepository.save(myBook);

        MyBookCreateRequest request = new MyBookCreateRequest(
                book.getIsbn(),
                book.getTitle(),
                book.getTitle(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getImageUrl(),
                List.of(),
                book.getPage()
        );

        // when // then
        assertThatThrownBy(() -> myBookService.create(user1.getId(), user1.getId(), request))
                .isInstanceOf(MyBookDuplicateException.class)
                .hasMessage("책장에 등록된 도서입니다.");
    }

    @DisplayName("등록한 도서의 상세 정보를 조회할 수 있다.")
    @Test
    void read() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book1 = createBook(1L, "book1");
        bookRepository.save(book1);

        MyBook myBook1 = createMyBook(room, book1, user);
        myBookRepository.save(myBook1);

        // when
        MyBookResponse response = myBookService.read(myBook1.getId());

        // then
        assertThat(response.id()).isEqualTo(myBook1.getId());
        assertThat(response.title()).isEqualTo(myBook1.getBook().getTitle());
    }

    @DisplayName("등록되지 않은 도서의 상세 정보를 조회하는 경우 예외가 발생한다.")
    @Test
    void readWhenIsNotRegisteredBook() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        Book book1 = createBook(1L, "book1");
        bookRepository.save(book1);

        MyBook myBook1 = createMyBook(room, book1, user);
        myBookRepository.save(myBook1);

        // when // then
        assertThatThrownBy(() -> myBookService.read(0L))
                .isInstanceOf(MyBookNotFoundException.class)
                .hasMessage("등록 도서를 찾을 수 없습니다.");
    }

    @DisplayName("사용자가 등록한 도서 목록을 최신 등록일 순으로 페이징 해서 조회할 수 있다. 첫 번째 페이지 조회.")
    @Test
    void readAllFirstPage() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
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

        MyBookCount myBookCount = createMyBookCount(5L, room, user);
        myBookCountRepository.save(myBookCount);


        // when
        MyBooksResponse myBooksResponse = myBookService.readAll(user.getId(), 3L, null);

        // then
        assertThat(myBooksResponse.count()).isEqualTo(5);

        List<MyBookResponse> myBookResponses = myBooksResponse.myBooks();
        assertThat(myBookResponses).hasSize(3)
                .extracting("id", "title")
                .containsExactly(
                        tuple(myBook5.getId(), book5.getTitle()),
                        tuple(myBook4.getId(), book4.getTitle()),
                        tuple(myBook3.getId(), book3.getTitle())
                );
    }

    @DisplayName("사용자가 등록한 도서 목록을 최신 등록일 순으로 페이징 해서 조회할 수 있다. 두 번째 ~ 마지막 페이지 조회.")
    @Test
    void readAllAfterFirstPage() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
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

        MyBookCount myBookCount = createMyBookCount(5L, room, user);
        myBookCountRepository.save(myBookCount);

        // when
        MyBooksResponse myBooksResponse = myBookService.readAll(user.getId(), 3L, myBook3.getId());

        // then
        assertThat(myBooksResponse.count()).isEqualTo(5);

        List<MyBookResponse> myBookResponses = myBooksResponse.myBooks();
        assertThat(myBookResponses).hasSize(2)
                .extracting("id", "title")
                .containsExactly(
                        tuple(myBook2.getId(), book2.getTitle()),
                        tuple(myBook1.getId(), book1.getTitle())
                );
    }

    @DisplayName("등록한 도서를 선택한만큼 삭제할 수 있다.")
    @Test
    void deleteIn() {

        // given
        User user = createUser("user@gmail.com", "user", "provId1");
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

        MyBookCount myBookCount = createMyBookCount(3L, room, user);
        myBookCountRepository.save(myBookCount);

        String ids = myBook1.getId() + "," + myBook2.getId();

        // when
        myBookService.delete(user.getId(), user.getId(), ids);

        // then
        boolean deleted1 = myBookRepository.findById(myBook1.getId()).isEmpty();
        boolean deleted2 = myBookRepository.findById(myBook2.getId()).isEmpty();
        assertThat(deleted1).isTrue();
        assertThat(deleted2).isTrue();

        Long count = myBookCountRepository.findById(myBookCount.getId())
                .map(MyBookCount::getCount)
                .orElse(0L);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("방의 주인이 아닌 사용자가 등록 도서를 삭제하는 경우 예외가 발생한다.")
    @Test
    void deleteInWhoIsNotOwnerOfRoom() {

        // given
        User user1 = createUser("user1@gmail.com", "user1", "provId1");
        User user2 = createUser("user2@gmail.com", "user2", "provId2");
        userRepository.saveAll(List.of(user1, user2));

        Room room = createRoom(user1);
        roomRepository.save(room);

        Book book1 = createBook(1L, "book1");
        bookRepository.save(book1);

        MyBook myBook1 = createMyBook(room, book1, user1);
        myBookRepository.save(myBook1);

        String ids = String.valueOf(myBook1.getId());

        // when // then
        assertThatThrownBy(() -> myBookService.delete(user2.getId(), user1.getId(), ids))
                .isInstanceOf(RoomAuthorizationException.class)
                .hasMessage("해당 방의 소유주가 아닙니다.");
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
                .build();
    }

    private Furniture createFurniture(Room room) {
        return Furniture.builder()
                .room(room)
                .furnitureType(FurnitureType.BOOKSHELF)
                .isVisible(false)
                .level(1)
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

    private MyBookCount createMyBookCount(Long count, Room room, User user) {
        return MyBookCount.builder()
                .count(count)
                .room(room)
                .user(user)
                .build();
    }
}