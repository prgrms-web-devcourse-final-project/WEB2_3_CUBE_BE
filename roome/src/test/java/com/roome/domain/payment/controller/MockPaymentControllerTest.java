package com.roome.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.domain.payment.dto.PaymentRequestDto;
import com.roome.domain.payment.dto.PaymentResponseDto;
import com.roome.domain.payment.dto.PaymentVerifyDto;
import com.roome.domain.payment.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MockPaymentController.class)
class MockPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void setAuthenticatedUser(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Test
    @DisplayName("결제 요청 - 정상 응답")
    void mockRequestPayment_Success() throws Exception {
        // given
        setAuthenticatedUser(1L);
        PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1000, 10);

        // when & then
        mockMvc.perform(post("/mock/payments/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is("order123")))
                .andExpect(jsonPath("$.amount", is(1000)))
                .andExpect(jsonPath("$.purchasedPoints", is(10)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }


    @Test
    @DisplayName("결제 검증 - 정상 검증")
    void mockVerifyPayment_Success() throws Exception {
        // given
        setAuthenticatedUser(1L);

        PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1000, 10);
        mockMvc.perform(post("/mock/payments/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk());

        PaymentVerifyDto verifyDto = new PaymentVerifyDto("paymentKey123", "order123", 1000);

        // when & then
        mockMvc.perform(post("/mock/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order123"))
                .andExpect(jsonPath("$.paymentKey").value("paymentKey123"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }


    @Test
    @DisplayName("결제 검증 실패 - 존재하지 않는 주문")
    void mockVerifyPayment_Fail_OrderNotFound() throws Exception {
        // given
        setAuthenticatedUser(1L);

        PaymentVerifyDto verifyDto = new PaymentVerifyDto("paymentKey123", "invalidOrder", 1000);

        // when & then
        mockMvc.perform(post("/mock/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("결제 실패 처리 - 정상 실패 처리")
    void mockFailPayment_Success() throws Exception {
        // given
        setAuthenticatedUser(1L);

        PaymentRequestDto requestDto = new PaymentRequestDto("order123", 1000, 10);
        mockMvc.perform(post("/mock/payments/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(post("/mock/payments/fail")
                        .param("orderId", "order123")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("결제 실패 처리 실패 - 존재하지 않는 주문")
    void mockFailPayment_Fail_OrderNotFound() throws Exception {
        // given
        setAuthenticatedUser(1L);

        // when & then
        mockMvc.perform(post("/mock/payments/fail")
                        .param("orderId", "invalidOrder")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

}
