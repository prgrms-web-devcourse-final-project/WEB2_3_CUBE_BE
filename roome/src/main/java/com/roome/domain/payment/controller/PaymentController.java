package com.roome.domain.payment.controller;

import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청 api
    // 사용자가 포인트 결제를 요청하면 db에 저장해줌
    @PostMapping("/request")
    public ResponseEntity<PaymentResponseDto> requestPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentRequestDto requestDto) {

        log.info("결제 요청: userId={}, orderId={}, amount={}", userId, requestDto.getOrderId(), requestDto.getAmount());

        PaymentResponseDto response = paymentService.requestPayment(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    // 클라이언트가 결제 완료 후 서버에서 검증 요청
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentVerifyDto verifyDto) {

        log.info("결제 검증 요청: userId={}, orderId={}, paymentKey={}", userId, verifyDto.getOrderId(), verifyDto.getPaymentKey());

        PaymentResponseDto response = paymentService.verifyPayment(userId, verifyDto);
        return ResponseEntity.ok(response);
    }

    // 결제 실패 처리
    @PostMapping("/fail/{orderId}")
    public ResponseEntity<Void> failPayment(@PathVariable String orderId) {

        log.warn("결제 실패 처리 요청: orderId={}", orderId);

        paymentService.failPayment(orderId);
        return ResponseEntity.noContent().build();
    }

    // 결제 취소 (환불) API
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @AuthenticationPrincipal Long userId,
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = "전액 취소") String cancelReason,
            @RequestParam(required = false) Integer cancelAmount) {

        log.info("결제 취소 요청: userId={}, orderId={}, cancelAmount={}", userId, orderId, cancelAmount);

        PaymentResponseDto response = paymentService.cancelPayment(userId, orderId, cancelReason, cancelAmount);
        return ResponseEntity.ok(response);
    }

}
