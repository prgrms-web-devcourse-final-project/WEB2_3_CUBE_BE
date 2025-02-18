package com.roome.domain.mybookreview.entity.repository;

import com.roome.domain.mybookreview.entity.MyBookReview;
import com.roome.domain.mybookreview.exception.MyBookReviewNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyBookReviewRepository extends JpaRepository<MyBookReview, Long> {

    default MyBookReview getById(Long id) {
        return findById(id)
                .orElseThrow(MyBookReviewNotFoundException::new);
    }

    Optional<MyBookReview> findByMyBookId(Long myBookId);
}
