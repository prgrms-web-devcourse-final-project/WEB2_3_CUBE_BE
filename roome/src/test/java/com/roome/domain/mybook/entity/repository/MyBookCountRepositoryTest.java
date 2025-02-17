package com.roome.domain.mybook.entity.repository;

import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ActiveProfiles("test")
@DataJpaTest
class MyBookCountRepositoryTest {

    @Autowired
    MyBookCountRepository myBookCountRepository;

    @Autowired
    RoomRepository roomRepository;

    @DisplayName("사용자가 등록한 도서의 개수를 1개 증가시킬 수 있다.")
    @Test
    void increase() {

        // given
        Room room = createRoom();
        roomRepository.save(room);

        MyBookCount myBookCount = createMyBookCount(1L, room);
        myBookCountRepository.save(myBookCount);

        // when
        int result = myBookCountRepository.increase(room.getId());

        // then
        assertThat(result).isEqualTo(1);

        myBookCount = myBookCountRepository.findById(myBookCount.getId()).orElseThrow();
        assertThat(myBookCount.getCount()).isEqualTo(2L);
    }

    Room createRoom() {
        return Room.builder()
                .user(null)
                .theme("theme")
                .createdAt(LocalDateTime.of(2025, 1, 1, 1, 1))
                .furnitures(null)
                .build();
    }

    MyBookCount createMyBookCount(Long count, Room room) {
        return MyBookCount.builder()
                .count(count)
                .room(room)
                .build();
    }
}