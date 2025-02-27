package com.roome.global.jwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenReissueRequest {

  private String refreshToken;
}
