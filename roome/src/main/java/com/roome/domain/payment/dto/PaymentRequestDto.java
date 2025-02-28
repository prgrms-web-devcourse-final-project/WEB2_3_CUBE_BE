package com.roome.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {

    @NotBlank(message = "orderId는 필수입니다.")
    private String orderId; // 주문 ID

    @NotNull(message = "결제 금액은 필수입니다.")
    private Integer amount; // 결제 금액

    @NotNull(message = "구매할 포인트는 필수입니다.")
    private Integer purchasedPoints; // 구매할 포인트

    public PaymentRequestDto(String orderId, Integer amount, Integer purchasedPoints) {
        this.orderId = orderId;
        this.amount = amount;
        this.purchasedPoints = purchasedPoints;
    }
}
