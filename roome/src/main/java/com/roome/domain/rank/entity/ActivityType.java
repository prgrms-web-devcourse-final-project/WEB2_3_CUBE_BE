package com.roome.domain.rank.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
  ATTENDANCE(1, "출석"),
  BOOK_REGISTRATION(10, "도서 등록"),
  BOOK_REVIEW(15, "서평 작성"),
  MUSIC_REGISTRATION(10, "음악 등록"),
  MUSIC_COMMENT(5, "음악 댓글"),
  ROOM_VISIT(1, "방 방문"),
  VISITOR_COUNT(2, "방문자 수"),
  GUESTBOOK(5, "방명록 작성");

  private final int score;
  private final String description;
}
