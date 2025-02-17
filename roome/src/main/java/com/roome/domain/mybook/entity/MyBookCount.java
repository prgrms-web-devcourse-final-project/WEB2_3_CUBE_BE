package com.roome.domain.mybook.entity;

import com.roome.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyBookCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long count;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    public static MyBookCount init(Room room) {
        MyBookCount myBookCount = new MyBookCount();
        myBookCount.count = 1L;
        myBookCount.room = room;
        return myBookCount;
    }
}
