package com.roome.domain.mybook.entity;

import com.roome.domain.book.entity.Book;
import com.roome.domain.mybook.exception.MyBookAuthorizationException;
import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    public static MyBook create(User user, Room room, Book book) {
        MyBook myBook = new MyBook();
        myBook.user = user;
        myBook.room = room;
        myBook.book = book;
        return myBook;
    }

    public void validateOwner(Long userId) {
        if (user == null || !user.getId().equals(userId)) {
            throw new MyBookAuthorizationException();
        }
    }
}
