package com.roome.domain.mycd.controller;

import com.roome.domain.mycd.dto.MyCdCreateRequest;
import com.roome.domain.mycd.dto.MyCdListResponse;
import com.roome.domain.mycd.dto.MyCdResponse;
import com.roome.domain.mycd.service.MyCdService;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.jwt.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MyCdController {

  private final MyCdService myCdService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;


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

  @GetMapping("/api/mycd/{myCdId}")
  public ResponseEntity<MyCdResponse> getMyCd(
      HttpServletRequest request,
      @PathVariable Long myCdId,
      @RequestParam(value = "userId", required = false) Long userId
  ) {
    Long authUserId = getUserIdFrom(request);

    // 요청한 userId가 없으면 본인의 CD 조회
    if (userId == null) {
      userId = authUserId;
    }

    // userId가 실제 존재하는지 검증 (예외 방지)
    userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    // 해당 CD가 userId의 소유인지 확인
    MyCdResponse response = myCdService.getMyCd(userId, myCdId);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/api/mycd")
  public ResponseEntity<Void> delete(
      @RequestParam("userId") Long userId,
      @RequestParam String myCdIds
  ) {
    myCdService.delete(userId, myCdIds);
    return ResponseEntity.ok().build();
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
