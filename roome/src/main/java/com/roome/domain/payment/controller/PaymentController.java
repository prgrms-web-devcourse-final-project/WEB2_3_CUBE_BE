package com.roome.domain.payment.controller;

import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 사용자가 포인트 결제를 요청하는 API
     */
    @PostMapping("/request")
    public ResponseEntity<PaymentResponseDto> requestPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentRequestDto requestDto) {

        PaymentResponseDto response = paymentService.requestPayment(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 성공 후, 결제 검증을 수행하는 API (토스 API 연동)
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentVerifyDto verifyDto) {

        PaymentResponseDto response = paymentService.verifyPayment(userId, verifyDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 실패 처리 API
     */
    @PostMapping("/fail/{orderId}")
    public ResponseEntity<Void> failPayment(@PathVariable String orderId) {
        paymentService.failPayment(orderId);
        return ResponseEntity.noContent().build();
    }
}
