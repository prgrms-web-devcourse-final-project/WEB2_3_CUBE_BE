package com.roome.domain.payment.dto;

import com.roome.domain.payment.entity.PaymentLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentLogResponseDto {

    private Long id;
    private int amount;
    private int earnedPoints;
    private String paymentKey;
    private LocalDateTime createdAt;

    public static PaymentLogResponseDto from(PaymentLog paymentLog) {
        return PaymentLogResponseDto.builder()
                .id(paymentLog.getId())
                .amount(paymentLog.getAmount())
                .earnedPoints(paymentLog.getEarnedPoints())
                .paymentKey(paymentLog.getPaymentKey())
                .createdAt(paymentLog.getCreatedAt())
                .build();
    }
}
