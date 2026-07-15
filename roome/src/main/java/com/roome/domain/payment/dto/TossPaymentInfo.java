package com.roome.domain.payment.dto;

import java.time.LocalDateTime;
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
    private final LocalDateTime approvedAt; // Toss가 확정한 승인 시각 (미승인 상태면 null)
    private final String method; // 결제 수단
    private final String receiptUrl; // 영수증 URL
    private final String approveNo; // PG(카드사) 승인 번호
}
