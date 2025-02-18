package com.roome.domain.mybook.service;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.repository.BookRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.mybook.service.response.MyBookResponse;
import com.roome.domain.mybook.service.response.MyBooksResponse;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.exception.DoNotHavePermissionToRoomException;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.roome.global.util.StringUtil.convertStringToList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyBookService {

    private final MyBookRepository myBookRepository;
    private final MyBookCountRepository myBookCountRepository;
    private final BookRepository bookRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public MyBookResponse create(Long userId, Long roomId, MyBookCreateRequest request) {
        User user = userRepository.getById(userId);
        Room room = roomRepository.getById(roomId);
        validateRoomOwner(room, userId);

        Book book = bookRepository.save(
                Book.create(
                        request.isbn(),
                        request.title(),
                        request.author(),
                        request.publisher(),
                        request.publishedDate(),
                        request.imageUrl(),
                        request.category(),
                        request.page()
                )
        );

        MyBook myBook = myBookRepository.save(MyBook.create(user, room, book));
        int result = myBookCountRepository.increase(roomId);
        if (result == 0) {
            myBookCountRepository.save(MyBookCount.init(room));
        }

        return MyBookResponse.from(myBook);
    }

    public MyBookResponse read(Long myBookId) {
        return MyBookResponse.from(
                myBookRepository.getById(myBookId)
        );
    }

    public MyBooksResponse readAll(Long roomId, Long pageSize, Long lastMyBookId) {
        List<MyBook> myBooks = lastMyBookId == null ?
                myBookRepository.findAll(roomId, pageSize) :
                myBookRepository.findAll(roomId, pageSize, lastMyBookId);
        return MyBooksResponse.of(myBooks, count(roomId));
    }

    @Transactional
    public void delete(Long userId, Long roomId, String myBookIds) {
        Room room = roomRepository.getById(roomId);
        validateRoomOwner(room, userId);

        List<String> ids = convertStringToList(myBookIds);
        myBookRepository.deleteAllIn(ids);
        myBookCountRepository.decrease(roomId, ids.size());
    }

    private void validateRoomOwner(Room room, Long userId) {
        if (!room.isCreatedBy(userId)) {
            throw new DoNotHavePermissionToRoomException();
        }
    }

    private Long count(Long roomId) {
        return myBookCountRepository.findByRoomId(roomId)
                .map(MyBookCount::getCount)
                .orElse(0L);
    }
}
