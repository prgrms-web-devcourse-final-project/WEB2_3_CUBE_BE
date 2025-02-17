package com.roome.domain.mybook.entity.repository;

import com.roome.domain.mybook.entity.MyBookCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MyBookCountRepository extends JpaRepository<MyBookCount, Long> {

    @Modifying(clearAutomatically = true)
    @Query(
            value = """
                    update my_book_count set count = count + 1 where room_id = :roomId
                    """,
            nativeQuery = true
    )
    int increase(@Param("roomId") Long roomId);

    Optional<MyBookCount> findByRoomId(@Param("roomId") Long roomId);
}
