package com.roome.domain.payment.entity;

import com.roome.domain.point.entity.PointReason;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 판매 중인 포인트 상품 카탈로그
// 가격과 사유 대응은 서버가 소유하고, 지급 포인트는 PointReason의 금액에서 파생
@Getter
@RequiredArgsConstructor
public enum PointProduct {

  POINT_100(1_000, PointReason.POINT_PURCHASE_100, PointReason.POINT_REFUND_100),
  POINT_550(5_000, PointReason.POINT_PURCHASE_550, PointReason.POINT_REFUND_550),
  POINT_1200(10_000, PointReason.POINT_PURCHASE_1200, PointReason.POINT_REFUND_1200),
  POINT_4000(30_000, PointReason.POINT_PURCHASE_4000, PointReason.POINT_REFUND_4000);

  private final int price; // 결제 금액 (KRW)
  private final PointReason earnReason; // 적립 사유
  private final PointReason refundReason; // 환불 사유

  // 지급 포인트는 사유의 금액에서 파생 (중복 정의 방지)
  public int getPoints() {
    return earnReason.getAmount();
  }

  public static Optional<PointProduct> findByPrice(int price) {
    return Arrays.stream(values())
        .filter(product -> product.price == price)
        .findFirst();
  }

  public static List<PointReason> purchaseReasons() {
    return Arrays.stream(values())
        .map(PointProduct::getEarnReason)
        .toList();
  }

  public static List<PointReason> refundReasons() {
    return Arrays.stream(values())
        .map(PointProduct::getRefundReason)
        .toList();
  }
}
