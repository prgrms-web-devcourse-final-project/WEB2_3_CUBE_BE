package com.roome.domain.point.entity;

import com.roome.domain.furniture.exception.BookshelfMaxLevelException;
import com.roome.domain.furniture.exception.CdRackMaxLevelException;

public enum PointReason {
  // 포인트 적립
  GUESTBOOK_REWARD(10),   // 방명록 작성 보상 (1일 1회)
  FIRST_COME_EVENT(200),  // 선착순 이벤트 보상
  DAILY_ATTENDANCE(400),  // 출석 체크 보상 (하루 1회)
  RANK_1(100),            // 주간 랭킹 1등
  RANK_2(70),             // 주간 랭킹 2등
  RANK_3(50),             // 주간 랭킹 3등

  // 포인트 사용
  THEME_PURCHASE(400),    // 테마 구매
  BOOK_UNLOCK_LV2(500),   // 도서 제한 해제 (21~30권)
  BOOK_UNLOCK_LV3(1500),  // 도서 제한 해제 (31권 이상)
  CD_UNLOCK_LV2(500),     // CD 제한 해제 (21~30개)
  CD_UNLOCK_LV3(1500),    // CD 제한 해제 (31개 이상)

  // 포인트 결제 (구매/환불) - 금액은 PointProduct 카탈로그와 일치한다
  POINT_PURCHASE_100(100),
  POINT_PURCHASE_550(550),
  POINT_PURCHASE_1200(1200),
  POINT_PURCHASE_4000(4000),

  POINT_REFUND_100(100),
  POINT_REFUND_550(550),
  POINT_REFUND_1200(1200),
  POINT_REFUND_4000(4000);

  private final int amount; // 해당 사유의 포인트 변동량 (단일 정의처)

  PointReason(int amount) {
    this.amount = amount;
  }

  public int getAmount() {
    return amount;
  }

  public static PointReason getBookShelfUpgradeReason(int level) {
    if (level == 1) {
      return BOOK_UNLOCK_LV2;
    }
    if (level == 2) {
      return BOOK_UNLOCK_LV3;
    }
    throw new BookshelfMaxLevelException();
  }

  public static PointReason getCdRackUpgradeReason(int level) {
    if (level == 1) {
      return CD_UNLOCK_LV2;
    }
    if (level == 2) {
      return CD_UNLOCK_LV3;
    }
    throw new CdRackMaxLevelException();
  }

}
