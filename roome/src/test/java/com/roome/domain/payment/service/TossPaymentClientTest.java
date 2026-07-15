package com.roome.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    @DisplayName("orderId 결제 조회 성공 - 응답 파싱")
    void findPaymentByOrderId_Success() throws Exception {
        // given
        String responseBody = """
        {
            "paymentKey": "pk123",
            "status": "DONE",
            "totalAmount": 5000
        }
        """;
        ResponseEntity<String> mockResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);
        when(objectMapper.readTree(responseBody)).thenReturn(new ObjectMapper().readTree(responseBody));

        // when
        var result = tossPaymentClient.findPaymentByOrderId("order123");

        // then
        assertTrue(result.isPresent());
        assertEquals("pk123", result.get().getPaymentKey());
        assertEquals("DONE", result.get().getStatus());
        assertEquals(5000, result.get().getTotalAmount());
    }

    @Test
    @DisplayName("orderId 결제 조회 - Toss에 기록이 없으면(404) 빈 Optional 반환")
    void findPaymentByOrderId_NotFound_ReturnsEmpty() {
        // given
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));

        // when
        var result = tossPaymentClient.findPaymentByOrderId("order123");

        // then
        assertTrue(result.isEmpty());
    }
}
