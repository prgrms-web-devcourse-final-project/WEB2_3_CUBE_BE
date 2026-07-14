package com.roome.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// Toss 결제 조회 API 응답 중 대사에 필요한 필드만 담는 DTO
@Getter
@Builder
@AllArgsConstructor
public class TossPaymentInfo {

    private final String paymentKey;
    private final String status; // DONE, CANCELED, READY, IN_PROGRESS, EXPIRED, ABORTED 등
    private final int totalAmount;
}
