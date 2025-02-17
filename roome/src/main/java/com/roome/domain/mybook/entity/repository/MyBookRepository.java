package com.roome.domain.mybook.entity.repository;

import com.roome.domain.mybook.entity.MyBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyBookRepository extends JpaRepository<MyBook, Long> {

    @Query(
            value = """
                    select mb
                    from MyBook mb
                    join fetch mb.book
                    where mb.room.id = :roomId
                    order by mb.id desc limit :limit
                    """
    )
    List<MyBook> findAll(
            @Param("roomId") Long roomId,
            @Param("limit") Long limit
    );

    @Query(
            value = """
                    select mb
                    from MyBook mb
                    join fetch mb.book
                    where mb.room.id = :roomId and mb.id < :lastMyBookId
                    order by mb.id desc limit :limit
                    """
    )
    List<MyBook> findAll(
            @Param("roomId") Long roomId,
            @Param("limit") Long limit,
            @Param("lastMyBookId") Long lastMyBookId
    );
}
