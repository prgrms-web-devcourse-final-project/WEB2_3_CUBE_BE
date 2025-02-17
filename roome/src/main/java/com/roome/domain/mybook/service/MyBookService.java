package com.roome.domain.mybook.service;

import com.roome.domain.book.entity.Book;
import com.roome.domain.book.entity.repository.BookRepository;
import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybook.service.request.MyBookCreateRequest;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void create(Long userId, Long roomId, MyBookCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();
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

        myBookRepository.save(MyBook.create(user, room, book));
        int result = myBookCountRepository.increase(roomId);
        if (result == 0) {
            myBookCountRepository.save(MyBookCount.init(room));
        }
    }
}
