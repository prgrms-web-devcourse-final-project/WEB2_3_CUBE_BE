package com.roome.domain.rank.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
  // 출석
  ATTENDANCE(1, "출석"),

  // 콘텐츠 등록
  BOOK_REGISTRATION(10, "도서 등록"),
  MUSIC_REGISTRATION(10, "음악 등록"),

  // 콘텐츠 상호작용
  BOOK_REVIEW(15, "서평 작성"),
  MUSIC_COMMENT(5, "음악 댓글"),

  // 방 활동
  ROOM_VISIT(1, "방 방문"),
  VISITOR_COUNT(2, "방문자 수"),
  GUESTBOOK(5, "방명록 작성"),

  // 소셜 활동
  FOLLOWER_INCREASE(5, "팔로워 증가");

  private final int score;
  private final String description;
}
