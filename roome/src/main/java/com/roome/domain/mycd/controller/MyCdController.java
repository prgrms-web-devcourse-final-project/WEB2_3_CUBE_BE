package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdRequest;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.service.MyCdService;
import com.roome.global.jwt.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyCdController {

  private final MyCdService myCdService;
  private final JwtTokenProvider jwtTokenProvider;


  @PostMapping("/api/mycd")
  public ResponseEntity<MyCdResponse> addMyCd(
      HttpServletRequest request,
      @RequestBody @Valid MyCdRequest myCdRequest) {
    Long userId = getUserIdFrom(request);  // ✅ MyBookController처럼 직접 토큰에서 userId 추출
    MyCdResponse response = myCdService.addCdToMyList(userId, myCdRequest.getCdId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  private Long getUserIdFrom(HttpServletRequest request) {
    String accessToken = request.getHeader("Authorization");
    if (StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer")) {
      accessToken = accessToken.substring(7);
    }
    Claims claims = jwtTokenProvider.parseClaims(accessToken);
    return Long.valueOf(claims.getSubject());
  }

}
