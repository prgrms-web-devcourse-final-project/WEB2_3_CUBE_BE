package com.roome.domain.mybookreview.service;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybook.exception.DoNotHavePermissionToMyBookException;
import com.roome.domain.mybookreview.entity.MyBookReview;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mybookreview.exception.MyBookReviewNotFoundException;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
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
        User user = userRepository.getById(userId);
        MyBook myBook = myBookRepository.getById(myBookId);
        validateMyBookOwner(myBook, userId);

        MyBookReview myBookReview = myBookReviewRepository.save(
                MyBookReview.create(
                        request.title(),
                        request.quote(),
                        request.takeaway(),
                        request.motivate(),
                        request.topic(),
                        request.freeFormText(),
                        request.coverColor(),
                        myBook,
                        user
                )
        );
        return MyBookReviewResponse.from(myBookReview);
    }

    public MyBookReviewResponse read(Long myBookId) {
        return MyBookReviewResponse.from(
                myBookReviewRepository.findByMyBookId(myBookId)
                        .orElseThrow(MyBookReviewNotFoundException::new)
        );
    }

    @Transactional
    public MyBookReviewResponse update(Long userId, Long myBookReviewId, MyBookReviewUpdateRequest request) {
        MyBookReview review = myBookReviewRepository.getById(myBookReviewId);
        review.validateOwner(userId);

        review.update(
                request.title(),
                request.quote(),
                request.takeaway(),
                request.motivate(),
                request.topic(),
                request.freeFormText(),
                request.coverColor()
        );
        return MyBookReviewResponse.from(review);
    }

    @Transactional
    public void delete(Long userId, Long myBookReviewId) {
        MyBookReview review = myBookReviewRepository.getById(myBookReviewId);
        review.validateOwner(userId);

        myBookReviewRepository.delete(review);
    }

    private void validateMyBookOwner(MyBook myBook, Long userId) {
        if (!myBook.isRegisteredBy(userId)) {
            throw new DoNotHavePermissionToMyBookException();
        }
    }
}
