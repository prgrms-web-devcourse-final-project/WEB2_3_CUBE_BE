package com.roome.domain.payment.dto;

import com.roome.domain.payment.entity.Payment;
import com.roome.domain.payment.entity.PaymentLog;
import com.roome.domain.payment.entity.PaymentStatus;
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
    private Long paymentId; // 연결된 결제 건 ID (매칭/추적용)
    private String orderId;  // 연결된 결제의 주문 ID
    private int amount;
    private int earnedPoints;
    private String paymentKey;
    private LocalDateTime createdAt;
    private PaymentStatus status;

    public static PaymentLogResponseDto from(PaymentLog paymentLog, PaymentStatus status) {
        Payment payment = paymentLog.getPayment();
        return PaymentLogResponseDto.builder()
                .id(paymentLog.getId())
                .paymentId(payment != null ? payment.getId() : null)
                .orderId(payment != null ? payment.getOrderId() : null)
                .amount(paymentLog.getAmount())
                .earnedPoints(paymentLog.getEarnedPoints())
                .paymentKey(paymentLog.getPaymentKey())
                .createdAt(paymentLog.getCreatedAt())
                .status(status)
                .build();
    }
}
