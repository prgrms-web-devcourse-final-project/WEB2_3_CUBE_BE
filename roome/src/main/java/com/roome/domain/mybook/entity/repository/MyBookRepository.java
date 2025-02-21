package com.roome.domain.mybook.entity.repository;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.exception.MyBookNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyBookRepository extends JpaRepository<MyBook, Long> {

    default MyBook getById(Long id) {
        return findById(id)
                .orElseThrow(MyBookNotFoundException::new);
    }

    @Query(
            value = """
                    select mb
                    from MyBook mb
                    join fetch mb.book
                    where mb.user.id = :roomOwnerId
                    order by mb.id desc limit :limit
                    """
    )
    List<MyBook> findAll(
            @Param("roomOwnerId") Long roomOwnerId,
            @Param("limit") Long limit
    );

    @Query(
            value = """
                    select mb
                    from MyBook mb
                    join fetch mb.book
                    where mb.user.id = :roomOwnerId and mb.id < :lastMyBookId
                    order by mb.id desc limit :limit
                    """
    )
    List<MyBook> findAll(
            @Param("roomOwnerId") Long roomOwnerId,
            @Param("limit") Long limit,
            @Param("lastMyBookId") Long lastMyBookId
    );

    @Modifying
    @Query(
            value = """
                    delete from MyBook mb where mb.id in(:ids)
                    """
    )
    void deleteAllIn(List<String> ids);
}
