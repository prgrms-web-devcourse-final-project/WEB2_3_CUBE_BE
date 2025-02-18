package com.roome.domain.mybook.entity.repository;

import com.roome.domain.mybook.entity.MyBookCount;
import com.roome.domain.mybook.entity.repository.MyBookCountRepository;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class MyBookCountRepositoryTest {

    @Autowired
    private MyBookCountRepository myBookCountRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("사용자가 등록한 도서의 개수를 1개 증가시킬 수 있다.")
    @Test
    void increase() {

        // given
        User user = createUser("user@gmail.com", "user");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        MyBookCount myBookCount = createMyBookCount(1L, room);
        myBookCountRepository.save(myBookCount);

        // when
        int result = myBookCountRepository.increase(room.getId());

        // then
        assertThat(result).isEqualTo(1);

        Long count = myBookCountRepository.findById(myBookCount.getId())
                .map(MyBookCount::getCount)
                .orElse(0L);
        assertThat(count).isEqualTo(2L);
    }

    @DisplayName("사용자가 등록한 도서의 개수를 지정한 수량만큼 감소시킬 수 있다.")
    @Test
    void decrease() {

        // given
        User user = createUser("user@gmail.com", "user");
        userRepository.save(user);

        Room room = createRoom(user);
        roomRepository.save(room);

        MyBookCount myBookCount = createMyBookCount(1L, room);
        myBookCountRepository.save(myBookCount);

        int count = 1;

        // when
        int result = myBookCountRepository.decrease(room.getId(), count);

        // then
        assertThat(result).isEqualTo(1);

        Long count1 = myBookCountRepository.findById(myBookCount.getId())
                .map(MyBookCount::getCount)
                .orElse(0L);
        assertThat(count1).isEqualTo(0L);
    }

    private User createUser(String email, String name) {
        return User.builder()
                .email(email)
                .name(name)
                .nickname("nickname")
                .profileImage("profile")
                .provider(Provider.GOOGLE)
                .providerId("provId")
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

    private MyBookCount createMyBookCount(Long count, Room room) {
        return MyBookCount.builder()
                .count(count)
                .room(room)
                .build();
    }
}