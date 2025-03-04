package com.roome.domain.mybookreview.service;

import com.roome.domain.mybook.entity.MyBook;
import com.roome.domain.mybook.entity.repository.MyBookRepository;
import com.roome.domain.mybookreview.entity.MyBookReview;
import com.roome.domain.mybookreview.entity.repository.MyBookReviewRepository;
import com.roome.domain.mybookreview.exception.MyBookReviewDuplicateException;
import com.roome.domain.mybookreview.exception.MyBookReviewNotFoundException;
import com.roome.domain.mybookreview.service.request.MyBookReviewCreateRequest;
import com.roome.domain.mybookreview.service.request.MyBookReviewUpdateRequest;
import com.roome.domain.mybookreview.service.response.MyBookReviewResponse;
import com.roome.domain.rank.entity.ActivityType;
import com.roome.domain.rank.service.UserActivityService;
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
  private final UserActivityService userActivityService;

  @Transactional
  public MyBookReviewResponse create(Long loginUserId, Long myBookId,
      MyBookReviewCreateRequest request) {
    User user = userRepository.getById(loginUserId);
    MyBook myBook = myBookRepository.getById(myBookId);
    myBook.validateOwner(loginUserId);

    myBookReviewRepository.findByMyBookId(myBook.getId())
        .ifPresent(exist -> {
          throw new MyBookReviewDuplicateException();
        });

    MyBookReview myBookReview = myBookReviewRepository.save(request.toEntity(myBook, user));

    // 서평 작성 활동 기록 추가 - 길이 체크
    userActivityService.recordUserActivity(
        loginUserId,
        ActivityType.BOOK_REVIEW,
        myBookId,
        request.freeFormText().length()  // 30자 이상 체크
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
  public MyBookReviewResponse update(Long loginUserId, Long myBookId,
      MyBookReviewUpdateRequest request) {
    MyBookReview review = myBookReviewRepository.findByMyBookId(myBookId)
        .orElseThrow(MyBookReviewNotFoundException::new);
    review.validateOwner(loginUserId);

    review.update(request.toEntity());
    return MyBookReviewResponse.from(review);
  }

  @Transactional
  public void delete(Long loginUserId, Long myBookId) {
    MyBookReview review = myBookReviewRepository.findByMyBookId(myBookId)
        .orElseThrow(MyBookReviewNotFoundException::new);
    review.validateOwner(loginUserId);

    myBookReviewRepository.delete(review);
  }
}
