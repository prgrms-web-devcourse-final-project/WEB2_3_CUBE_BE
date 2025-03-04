package com.roome.domain.mybookreview.entity.repository;

import com.roome.domain.mybookreview.entity.MyBookReview;
import com.roome.domain.mybookreview.exception.MyBookReviewNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MyBookReviewRepository extends JpaRepository<MyBookReview, Long> {

    default MyBookReview getById(Long id) {
        return findById(id)
                .orElseThrow(MyBookReviewNotFoundException::new);
    }

    Optional<MyBookReview> findByMyBookId(Long myBookId);

    Long countByUserId(Long userId);

    // 특정 사용자가 작성한 모든 도서 리뷰 삭제
    void deleteAllByUserId(Long userId);

    @Modifying
    @Query(
            value = """
                    delete from MyBookReview mbr where mbr.myBook.id in(:myBookIds)
                    """
    )
    void deleteAllByMyBookIds(List<String> myBookIds);
}
