package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyCdController {

  private final MyCdService myCdService;
  private final JwtTokenProvider jwtTokenProvider;


  @PostMapping("/api/mycd")
  public ResponseEntity<MyCdResponse> addMyCd(
      HttpServletRequest request,
      @RequestBody @Valid MyCdCreateRequest myCdRequest) {
    Long userId = getUserIdFrom(request);
    MyCdResponse response = myCdService.addCdToMyList(userId, myCdRequest.getCdId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/api/mycd")
  public ResponseEntity<MyCdListResponse> getMyCdList(
      HttpServletRequest request,
      @RequestParam(value = "userId", required = false) Long userId
  ) {
    Long authUserId = getUserIdFrom(request);

    // 요청한 userId가 없으면 본인의 CD 목록 조회
    if (userId == null) {
      userId = authUserId;
    }

    // userId가 실제 존재하는지 검증 (예외 방지)
    userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    return ResponseEntity.ok(myCdService.getMyCdList(userId));
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
