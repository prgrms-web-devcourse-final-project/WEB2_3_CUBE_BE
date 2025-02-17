package com.roome.domain.mybookreview.entity.repository;

import com.roome.domain.mybookreview.entity.MyBookReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyBookReviewRepository extends JpaRepository<MyBookReview, Long> {

    Optional<MyBookReview> findByMyBookId(Long myBookId);
}
