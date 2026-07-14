package com.roome.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {

    @NotBlank(message = "orderId는 필수입니다.")
    // Toss 스펙과 동일한 형식 제약 (6~64자, 영문/숫자/-/_)
    @Pattern(regexp = "^[a-zA-Z0-9_-]{6,64}$", message = "orderId는 6~64자의 영문, 숫자, '-', '_'만 허용됩니다.")
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
