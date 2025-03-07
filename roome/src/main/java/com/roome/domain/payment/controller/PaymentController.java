package com.roome.domain.payment.controller;

import com.roome.domain.payment.dto.PaymentLogResponseDto;
import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "결제 API", description = "포인트 결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청 api
    // 사용자가 포인트 결제를 요청하면 db에 저장해줌
    @PostMapping("/request")
    @Operation(summary = "결제 요청", description = "사용자가 포인트 결제를 요청하면 결제 정보를 DB에 저장한다.")
    public ResponseEntity<PaymentResponseDto> requestPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentRequestDto requestDto) {

        log.info("결제 요청: userId={}, orderId={}, amount={}", userId, requestDto.getOrderId(), requestDto.getAmount());

        PaymentResponseDto response = paymentService.requestPayment(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    // 클라이언트가 결제 완료 후 서버에서 검증 요청
    @PostMapping("/verify")
    @Operation(summary = "결제 검증", description = "클라이언트가 결제 완료 후, 결제 정보를 검증한다.")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentVerifyDto verifyDto) {

        log.info("결제 검증 요청: userId={}, orderId={}, paymentKey={}", userId, verifyDto.getOrderId(), verifyDto.getPaymentKey());

        PaymentResponseDto response = paymentService.verifyPayment(userId, verifyDto);
        return ResponseEntity.ok(response);
    }

    // 결제 실패 처리
    @PostMapping("/fail/{orderId}")
    @Operation(summary = "결제 실패 처리", description = "결제가 실패한 경우 해당 결제 정보를 실패 상태로 업데이트한다.")
    public ResponseEntity<Void> failPayment(@PathVariable String orderId) {

        log.warn("결제 실패 처리 요청: orderId={}", orderId);

        paymentService.failPayment(orderId);
        return ResponseEntity.noContent().build();
    }

    // 결제 취소 (환불) API
    @PostMapping("/cancel")
    @Operation(summary = "결제 취소 (환불)", description = "결제 취소 요청을 처리하고 환불을 진행한다.")
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @AuthenticationPrincipal Long userId,
            @RequestParam String paymentKey,
            @RequestParam(required = false, defaultValue = "전액 취소") String cancelReason,
            @RequestParam(required = false) Integer cancelAmount) {

        log.info("결제 취소 요청: userId={}, paymentKey={}, cancelAmount={}", userId, paymentKey, cancelAmount);

        PaymentResponseDto response = paymentService.cancelPayment(userId, paymentKey, cancelReason, cancelAmount);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 내역 조회", description = "사용자의 결제 내역을 최신순으로 조회합니다.")
    @GetMapping("/history")
    public ResponseEntity<List<PaymentLogResponseDto>> getPaymentHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<PaymentLogResponseDto> paymentLogs = paymentService.getPaymentHistory(userId, page, size);
        return ResponseEntity.ok(paymentLogs);
    }

}
