package com.roome.domain.mybook.entity;

import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyBookCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long count;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static MyBookCount init(Room room, User user) {
        MyBookCount myBookCount = new MyBookCount();
        myBookCount.count = 1L;
        myBookCount.room = room;
        myBookCount.user = user;
        return myBookCount;
    }
}
