package com.roome.global.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenReissueRequest {

  private String refreshToken;

  public String getRefreshToken() {
    return refreshToken != null ? refreshToken : "";
  }
}
