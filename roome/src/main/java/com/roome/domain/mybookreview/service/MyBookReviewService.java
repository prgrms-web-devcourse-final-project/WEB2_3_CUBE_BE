package com.roome.domain.mybookreview.service;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybookreview.entity.MyBookReview;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MyBookReviewService {

    private final MyBookReviewRepository myBookReviewRepository;
    private final MyBookRepository myBookRepository;
    private final UserRepository userRepository;

    @Transactional
    public MyBookReviewResponse create(Long userId, Long myBookId, MyBookReviewCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        MyBook myBook = myBookRepository.findById(myBookId).orElseThrow();
        if (!myBook.isRegisteredBy(userId)) {
            throw new IllegalArgumentException();
        }

        MyBookReview myBookReview = myBookReviewRepository.save(
                MyBookReview.create(
                        request.title(),
                        request.quote(),
                        request.takeaway(),
                        request.freeFormText(),
                        request.coverColor(),
                        myBook,
                        user
                )
        );
        return MyBookReviewResponse.from(myBookReview);
    }
}
