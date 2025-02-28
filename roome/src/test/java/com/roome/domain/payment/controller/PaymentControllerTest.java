package com.roome.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.entity.PaymentStatus;
import com.roome.domain.payment.service.PaymentService;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private void setAuthenticatedUser(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Test
    @DisplayName("결제 요청이 정상적으로 수행되어야 한다.")
    void requestPayment_Success() throws Exception {
        // given
        setAuthenticatedUser(1L);
        PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1000, 10);
        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .orderId("order123")
                .amount(1000)
                .purchasedPoints(10)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentService.requestPayment(any(Long.class), any(PaymentRequestDto.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/payments/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order123"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(paymentService, times(1)).requestPayment(any(Long.class), any(PaymentRequestDto.class));
    }


    @Test
    @DisplayName("결제 검증이 정상적으로 수행되어야 한다.")
    void verifyPayment_Success() throws Exception {
        // given
        setAuthenticatedUser(1L);
        PaymentVerifyDto verifyDto = new PaymentVerifyDto("paymentKey123", "order123", 1000);
        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .orderId("order123")
                .paymentKey("paymentKey123")
                .amount(1000)
                .purchasedPoints(10)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentService.verifyPayment(any(Long.class), any(PaymentVerifyDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order123"))
                .andExpect(jsonPath("$.paymentKey").value("paymentKey123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(paymentService, times(1)).verifyPayment(any(Long.class), any(PaymentVerifyDto.class));
    }

    @Test
    @DisplayName("결제 검증 시 주문 ID가 존재하지 않으면 예외가 발생해야 한다.")
    void verifyPayment_OrderNotFound() throws Exception {
        // given
        setAuthenticatedUser(1L);
        PaymentVerifyDto verifyDto = new PaymentVerifyDto("paymentKey123", "invalidOrder", 1000);

        when(paymentService.verifyPayment(any(Long.class), any(PaymentVerifyDto.class)))
                .thenThrow(new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).verifyPayment(any(Long.class), any(PaymentVerifyDto.class));
    }

    @Test
    @DisplayName("결제 검증 시 결제 금액이 일치하지 않으면 예외가 발생해야 한다.")
    void verifyPayment_AmountMismatch() throws Exception {
        // given
        setAuthenticatedUser(1L);
        PaymentVerifyDto verifyDto = new PaymentVerifyDto("paymentKey123", "order123", 2000); // 다른 금액

        doThrow(new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH))
                .when(paymentService).verifyPayment(any(Long.class), any(PaymentVerifyDto.class));

        // when & then
        mockMvc.perform(post("/api/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).verifyPayment(any(Long.class), any(PaymentVerifyDto.class));
    }


    @Test
    @DisplayName("결제 검증 요청이 실패하면 예외가 발생해야 한다.")
    void verifyPayment_FailedVerification() throws Exception {
        // given
        setAuthenticatedUser(1L);
        PaymentVerifyDto verifyDto = new PaymentVerifyDto("paymentKey123", "order123", 1000);

        doThrow(new BusinessException(ErrorCode.PAYMENT_VERIFICATION_FAILED))
                .when(paymentService).verifyPayment(any(Long.class), any(PaymentVerifyDto.class));

        // when & then
        mockMvc.perform(post("/api/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).verifyPayment(any(Long.class), any(PaymentVerifyDto.class));
    }


    @Test
    @DisplayName("결제 처리 중 서버 오류가 발생하면 예외가 발생해야 한다.")
    void requestPayment_InternalServerError() throws Exception {
        // given
        setAuthenticatedUser(1L);
        PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1000, 10);

        doThrow(new BusinessException(ErrorCode.PAYMENT_PROCESSING_ERROR))
                .when(paymentService).requestPayment(any(Long.class), any(PaymentRequestDto.class));

        // when & then
        mockMvc.perform(post("/api/payments/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(paymentService, times(1)).requestPayment(any(Long.class), any(PaymentRequestDto.class));
    }


    @Test
    @DisplayName("사용자가 다른 사람의 결제 정보에 접근하려 하면 예외가 발생해야 한다.")
    void verifyPayment_AccessDenied() throws Exception {
        // given
        setAuthenticatedUser(2L);
        PaymentVerifyDto verifyDto = new PaymentVerifyDto("paymentKey123", "order123", 1000);

        when(paymentService.verifyPayment(any(Long.class), any(PaymentVerifyDto.class)))
                .thenThrow(new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED));

        // when & then
        mockMvc.perform(post("/api/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(paymentService, times(1)).verifyPayment(any(Long.class), any(PaymentVerifyDto.class));
    }



    @Test
    @DisplayName("결제 실패 처리가 정상적으로 수행되어야 한다.")
    void failPayment_Success() throws Exception {
        // given
        setAuthenticatedUser(1L);
        doNothing().when(paymentService).failPayment("order123");

        // when & then
        mockMvc.perform(post("/api/payments/fail/order123")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(paymentService, times(1)).failPayment("order123");
    }


    @Test
    @DisplayName("결제 실패 처리 시 주문 ID가 존재하지 않으면 예외가 발생해야 한다.")
    void failPayment_OrderNotFound() throws Exception {
        // given
        setAuthenticatedUser(1L);
        doThrow(new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)).when(paymentService).failPayment("invalidOrder");

        // when & then
        mockMvc.perform(post("/api/payments/fail/invalidOrder")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).failPayment("invalidOrder");
    }

}
