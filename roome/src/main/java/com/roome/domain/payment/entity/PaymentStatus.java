package com.roome.domain.payment.entity;

public enum PaymentStatus {
    PENDING, SUCCESS, FAILED, CANCELED;

    // 허용된 상태 전이 규칙 (결제 원장의 상태 머신)
    //  PENDING -> SUCCESS (승인 완결) / FAILED(실패 or 미완료) / CANCELED (승인 전 PG 취소)
    //  SUCCESS -> CANCELED (환불)
    //  FAILED, CANCELED = 종료 상태 (전이 불가)
    public boolean canTransitionTo(PaymentStatus target) {
        return switch (this) {
            case PENDING -> target == SUCCESS || target == FAILED || target == CANCELED;
            case SUCCESS -> target == CANCELED;
            case FAILED, CANCELED -> false;
        };
    }
}
