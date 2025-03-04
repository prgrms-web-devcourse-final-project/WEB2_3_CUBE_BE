package com.roome.domain.mybook.service;

import static com.roome.global.util.StringUtil.convertStringToList;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.BookGenre;
import com.roome.domain.book.entity.Genre;
import com.roome.domain.book.entity.repository.BookRepository;
import com.roome.domain.book.entity.repository.GenreRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybook.exception.MyBookDuplicateException;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyBookService {

    private final MyBookRepository myBookRepository;
    private final MyBookReviewRepository myBookReviewRepository;
    private final MyBookCountRepository myBookCountRepository;
    private final BookRepository bookRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final UserActivityService userActivityService;

    @Transactional
    public MyBookResponse create(Long loginUserId, Long roomOwnerId, MyBookCreateRequest request) {
        User loginUser = userRepository.getById(loginUserId);
        Room room = roomRepository.getByUserId(roomOwnerId);
        room.validateOwner(loginUserId);
        room.checkBookshelfIsFull(count(roomOwnerId));

        Book bookEntity = request.toBookEntity();
        Book book = bookRepository.findByIsbn(bookEntity.getIsbn())
                .orElseGet(() -> {
                    addGenres(bookEntity, request.genreNames());
                    return bookRepository.save(bookEntity);
                });

        myBookRepository.findByRoomIdAndBookId(room.getId(), book.getId())
                .ifPresent(exist -> { throw new MyBookDuplicateException(); });

        MyBook myBook = myBookRepository.save(MyBook.create(loginUser, room, book));
        int result = myBookCountRepository.increase(roomOwnerId);
        if (result == 0) {
            myBookCountRepository.save(MyBookCount.init(room, loginUser));
        }

    // 도서 등록 활동 기록 추가
    userActivityService.recordUserActivity(loginUserId, ActivityType.BOOK_REGISTRATION,
        book.getId());

    return MyBookResponse.from(myBook);
  }

    public MyBookResponse read(Long myBookId) {
        return MyBookResponse.from(
                myBookRepository.getById(myBookId)
        );
    }

    public MyBooksResponse readAll(Long roomOwnerId, Long pageSize, Long lastMyBookId) {
        List<MyBook> myBooks = lastMyBookId == null ?
                myBookRepository.findAll(roomOwnerId, pageSize) :
                myBookRepository.findAll(roomOwnerId, pageSize, lastMyBookId);
        return MyBooksResponse.of(myBooks, count(roomOwnerId));
    }

    @Transactional
    public void delete(Long loginUserId, Long roomOwnerId, String myBookIds) {
        Room room = roomRepository.getByUserId(roomOwnerId);
        room.validateOwner(loginUserId);

        List<String> ids = convertStringToList(myBookIds);
        myBookReviewRepository.deleteAllByMyBookIds(ids);
        myBookRepository.deleteAllIn(ids);
        myBookCountRepository.decrease(roomOwnerId, ids.size());
    }

    private Long count(Long roomOwnerId) {
        return myBookCountRepository.findByUserId(roomOwnerId)
                .map(MyBookCount::getCount)
                .orElse(0L);
    }

    private void addGenres(Book book, List<String> genreNames) {
        List<Genre> genres = findGenresFrom(genreNames);
        for (Genre genre : genres) {
            book.addBookGenre(
                    BookGenre.builder().book(book).genre(genre).build()
            );
        }
    }

    private List<Genre> findGenresFrom(List<String> genreNames) {
        return genreNames.stream()
                .map(genreName -> genreRepository.findByName(genreName)
                        .orElseGet(() -> {
                            Genre genre = Genre.create(genreName);
                            genreRepository.save(genre);
                            return genre;
                        })
                ).toList();
    }
}
