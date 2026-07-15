package com.roome.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

  @Test
  @DisplayName("RestTemplate은 무한 대기를 막기 위해 연결 및 응답 타임아웃이 설정되어 있어야 한다.")
  void restTemplate_HasTimeouts() {
    // when
    RestTemplate restTemplate = new RestTemplateConfig().restTemplate();
    ClientHttpRequestFactory factory = restTemplate.getRequestFactory();

    // then: 기본(new RestTemplate())이 아닌, 타임아웃이 지정된 팩토리여야 한다
    assertThat(factory).isInstanceOf(SimpleClientHttpRequestFactory.class);
    assertThat(ReflectionTestUtils.getField(factory, "connectTimeout"))
        .isEqualTo((int) RestTemplateConfig.CONNECT_TIMEOUT.toMillis());
    assertThat(ReflectionTestUtils.getField(factory, "readTimeout"))
        .isEqualTo((int) RestTemplateConfig.READ_TIMEOUT.toMillis());
  }
}
