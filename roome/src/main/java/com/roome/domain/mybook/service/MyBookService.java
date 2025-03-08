package com.roome.domain.mybook.service;

import static com.roome.global.util.StringUtil.convertStringToList;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.BookGenre;
import com.roome.domain.book.entity.Genre;
import com.roome.domain.book.entity.repository.BookRepository;
import com.roome.domain.book.entity.repository.GenreRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.MyBookQueryModel;
import com.roome.domain.mybook.entity.repository.*;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybook.event.BookCollectionEvent;
import com.roome.domain.mybook.exception.MyBookDuplicateException;
import com.roome.domain.mybook.exception.MyBookNotFoundException;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewQueryModelRedisRepository;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.RankingService;
import com.roome.domain.rank.service.UserActivityService;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyBookService {

    private final MyBookQueryModelRedisRepository myBookQueryModelRedisRepository;
    private final MyBookCountRedisRepository myBookCountRedisRepository;
    private final MyBookIdsRedisRepository myBookIdsRedisRepository;
    private final MyBookReviewQueryModelRedisRepository myBookReviewQueryModelRedisRepository;

    private final MyBookRepository myBookRepository;
    private final MyBookReviewRepository myBookReviewRepository;
    private final MyBookCountRepository myBookCountRepository;
    private final BookRepository bookRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final UserActivityService userActivityService;
    private final RankingService rankingService;
    private final ApplicationEventPublisher eventPublisher; // 이벤트 발행을 위해 추가

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
                .ifPresent(exist -> {
                    throw new MyBookDuplicateException();
                });

        MyBook myBook = myBookRepository.save(MyBook.create(loginUser, room, book));
        int result = myBookCountRepository.increase(roomOwnerId);
        if (result == 0) {
            myBookCountRepository.save(MyBookCount.init(room, loginUser));
        }

        if (rankingService.isRanker(roomOwnerId)) {
            myBookQueryModelRedisRepository.create(MyBookQueryModel.create(myBook), Duration.ofHours(1));
            myBookIdsRedisRepository.add(roomOwnerId, myBook.getId(), Duration.ofHours(1));
            myBookCountRedisRepository.createOrUpdate(roomOwnerId, count(roomOwnerId) + 1, Duration.ofHours(1));
        }

        // 도서 등록 활동 기록 추가
        userActivityService.recordUserActivity(loginUserId, ActivityType.BOOK_REGISTRATION, book.getId());

        // 이벤트 발행
        eventPublisher.publishEvent(new BookCollectionEvent.BookAddedEvent(this, loginUserId));
        log.debug("북 추가 완료 후 이벤트 발행 user: {}", loginUserId);
      
        return MyBookResponse.from(MyBookQueryModel.create(myBook));
    }

    public MyBookResponse read(Long myBookId) {
        return MyBookResponse.from(
                myBookQueryModelRedisRepository.read(myBookId)
                        .orElseGet(() -> fetch(myBookId))
        );
    }

    public MyBooksResponse readAll(Long roomOwnerId, Long pageSize, Long lastMyBookId, String keyword) {
        keyword = keyword == null || keyword.isBlank() ? null : keyword.toLowerCase();
        if (keyword == null && rankingService.isRanker(roomOwnerId)) {
            return MyBooksResponse.of(
                    readAll(readAllMyBookIds(roomOwnerId, pageSize, lastMyBookId, keyword)),
                    count(roomOwnerId)
            );
        }
        List<MyBook> myBooks = lastMyBookId == null ?
                myBookRepository.findAll(roomOwnerId, pageSize, keyword) :
                myBookRepository.findAll(roomOwnerId, pageSize, lastMyBookId, keyword);
        return MyBooksResponse.of(
                myBooks.stream().map(MyBookQueryModel::create).toList(),
                count(roomOwnerId)
        );
    }

    @Transactional
    public void delete(Long loginUserId, Long roomOwnerId, String myBookIds) {
        Room room = roomRepository.getByUserId(roomOwnerId);
        room.validateOwner(loginUserId);

        List<String> ids = convertStringToList(myBookIds);
        myBookReviewRepository.deleteAllByMyBookIds(ids);
        myBookRepository.deleteAllIn(ids);
        myBookCountRepository.decrease(roomOwnerId, ids.size());
      
        //이벤트 발행
        eventPublisher.publishEvent(new BookCollectionEvent.BookRemovedEvent(this, loginUserId));
        log.debug("북 삭제 완료 후 이벤트 발행 user: {}", loginUserId);

        if (rankingService.isRanker(roomOwnerId)) {
            myBookIdsRedisRepository.delete(roomOwnerId, ids);
            ids.forEach(myBookId -> {
                myBookReviewQueryModelRedisRepository.delete(Long.valueOf(myBookId));
                myBookQueryModelRedisRepository.delete(Long.valueOf(myBookId));
            });
            myBookCountRedisRepository.createOrUpdate(roomOwnerId, count(roomOwnerId) - ids.size(), Duration.ofHours(1));
        }
    }

    private MyBookQueryModel fetch(Long myBookId) {
        MyBook myBook = myBookRepository.findFetchById(myBookId)
                .orElseThrow(MyBookNotFoundException::new);
        if (rankingService.isRanker(myBook.getUser().getId())) {
            myBookQueryModelRedisRepository.create(MyBookQueryModel.create(myBook), Duration.ofHours(1));
        }
        return MyBookQueryModel.create(myBook);
    }

    private List<MyBookQueryModel> readAll(List<Long> myBookIds) {
        Map<Long, MyBookQueryModel> myBookMap = myBookQueryModelRedisRepository.readAll(myBookIds);
        return myBookIds.stream()
                .map(myBookId -> myBookMap.containsKey(myBookId) ?
                        myBookMap.get(myBookId) :
                        fetch(myBookId))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Long> readAllMyBookIds(Long roomOwnerId, Long pageSize, Long lastMyBookId, String keyword) {
        List<Long> myBookIds = myBookIdsRedisRepository.readAll(roomOwnerId, pageSize, lastMyBookId, keyword);
        if (pageSize == myBookIds.size()) {
            return myBookIds;
        }
        List<MyBook> myBooks = lastMyBookId == null ?
                myBookRepository.findAll(roomOwnerId, pageSize, keyword) :
                myBookRepository.findAll(roomOwnerId, pageSize, lastMyBookId, keyword);
        return myBooks.stream().map(MyBook::getId).toList();
    }

    private Long count(Long roomOwnerId) {
        Long result = myBookCountRedisRepository.read(roomOwnerId);
        if (result != null) {
            return result;
        }
        Long count = myBookCountRepository.findByUserId(roomOwnerId)
                .map(MyBookCount::getCount)
                .orElse(0L);
        if (rankingService.isRanker(roomOwnerId)) {
            myBookCountRedisRepository.createOrUpdate(roomOwnerId, count, Duration.ofHours(1));
        }
        return count;
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
