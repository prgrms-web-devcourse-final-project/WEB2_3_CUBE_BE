package com.roome.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TossPaymentClientTest {

    @InjectMocks
    private TossPaymentClient tossPaymentClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private final String testSecretKey = "test_secret_key";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(tossPaymentClient, "secretKey", testSecretKey);
    }

    @Test
    @DisplayName("결제 검증 성공 - 정상 응답")
    void verifyPayment_Success() throws Exception {
        // given
        String paymentKey = "paymentKey123";
        String orderId = "order123";
        int amount = 1000;
        String responseBody = """
        {
            "status": "DONE",
            "amount": 1000
        }
        """;

        ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        JsonNode mockJsonNode = new ObjectMapper().readTree(responseBody);
        when(objectMapper.readTree(responseBody)).thenReturn(mockJsonNode);

        // when
        boolean result = tossPaymentClient.verifyPayment(paymentKey, orderId, amount);

        // then
        assertTrue(result);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        verify(objectMapper, times(1)).readTree(responseBody); // ObjectMapper 호출 확인
    }


    @Test
    @DisplayName("결제 검증 실패 - 결제 금액 불일치")
    void verifyPayment_Fail_AmountMismatch() throws Exception {
        // given
        String paymentKey = "paymentKey123";
        String orderId = "order123";
        int expectedAmount = 1000;
        int actualAmount = 500;
        String responseBody = """
        {
            "status": "DONE",
            "amount": 500
        }
        """;

        ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        JsonNode mockJsonNode = new ObjectMapper().readTree(responseBody);
        when(objectMapper.readTree(responseBody)).thenReturn(mockJsonNode);

        // when
        boolean result = tossPaymentClient.verifyPayment(paymentKey, orderId, expectedAmount);

        // then
        assertFalse(result);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        verify(objectMapper, times(1)).readTree(responseBody);
    }


    @Test
    @DisplayName("결제 검증 실패 - 결제 상태가 DONE이 아님")
    void verifyPayment_Fail_StatusNotDone() throws Exception {
        // given
        String paymentKey = "paymentKey123";
        String orderId = "order123";
        int amount = 1000;
        String responseBody = """
        {
            "status": "CANCELED",
            "amount": 1000
        }
        """;

        ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        JsonNode mockJsonNode = new ObjectMapper().readTree(responseBody);
        when(objectMapper.readTree(responseBody)).thenReturn(mockJsonNode);

        // when
        boolean result = tossPaymentClient.verifyPayment(paymentKey, orderId, amount);

        // then
        assertFalse(result);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        verify(objectMapper, times(1)).readTree(responseBody);
    }


    @Test
    @DisplayName("결제 검증 실패 - API 응답이 4xx 에러 발생")
    void verifyPayment_Fail_ClientError() {
        // given
        String paymentKey = "paymentKey123";
        String orderId = "order123";
        int amount = 1000;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // when & then
        assertThrows(BusinessException.class, () -> tossPaymentClient.verifyPayment(paymentKey, orderId, amount));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("결제 검증 실패 - API 응답이 5xx 서버 오류 발생")
    void verifyPayment_Fail_ServerError() {
        // given
        String paymentKey = "paymentKey123";
        String orderId = "order123";
        int amount = 1000;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> tossPaymentClient.verifyPayment(paymentKey, orderId, amount));

        assertEquals(ErrorCode.PAYMENT_VERIFICATION_FAILED, exception.getErrorCode());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }
}
