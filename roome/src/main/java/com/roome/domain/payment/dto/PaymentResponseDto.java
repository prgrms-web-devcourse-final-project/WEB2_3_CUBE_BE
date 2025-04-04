package com.roome.domain.payment.dto;

import com.roome.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PaymentResponseDto {

    private String orderId; // 우리 서비스 내부 주문 ID
    private String paymentKey; // 토스 결제 고유 키
    private int amount; // 결제 금액
    private int purchasedPoints; // 구매한 포인트
    private PaymentStatus status; // 결제 상태

}
