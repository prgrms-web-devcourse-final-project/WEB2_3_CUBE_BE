package com.roome.domain.mybook.entity.repository;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.exception.MyBookNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MyBookRepository extends JpaRepository<MyBook, Long> {

    default MyBook getById(Long id) {
        return findById(id)
                .orElseThrow(MyBookNotFoundException::new);
    }

    Optional<MyBook> findByRoomIdAndBookId(Long roomId, Long bookId);

    @Query(
            value = """
                    select mb
                    from MyBook mb
                    join fetch mb.book
                    where mb.user.id = :roomOwnerId
                    and (:keyword is null or
                    lower(mb.book.title) like concat('%', :keyword, '%')
                    or lower(mb.book.author) like concat('%', :keyword, '%')
                    or lower(mb.book.publisher) like concat('%', :keyword, '%'))
                    order by mb.id desc limit :limit
                    """
    )
    List<MyBook> findAll(
            @Param("roomOwnerId") Long roomOwnerId,
            @Param("limit") Long limit,
            @Param("keyword") String keyword
    );

    @Query(
            value = """
                    select mb
                    from MyBook mb
                    join fetch mb.book
                    where mb.user.id = :roomOwnerId and mb.id < :lastMyBookId
                    and (:keyword is null or
                    lower(mb.book.title) like concat('%', :keyword, '%')
                    or lower(mb.book.author) like concat('%', :keyword, '%')
                    or lower(mb.book.publisher) like concat('%', :keyword, '%'))
                    order by mb.id desc limit :limit
                    """
    )
    List<MyBook> findAll(
            @Param("roomOwnerId") Long roomOwnerId,
            @Param("limit") Long limit,
            @Param("lastMyBookId") Long lastMyBookId,
            @Param("keyword") String keyword
    );

    @Modifying
    @Query(
            value = """
                    delete from MyBook mb where mb.id in(:ids)
                    """
    )
    void deleteAllIn(List<String> ids);
    // 특정 사용자가 가지고 있는 모든 도서 목록 조회
    List<MyBook> findAllByUserId(Long userId);
}
