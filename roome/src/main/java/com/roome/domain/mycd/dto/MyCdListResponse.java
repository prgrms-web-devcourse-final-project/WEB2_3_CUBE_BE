package com.roome.domain.mycd.dto;

import com.roome.domain.mycd.entity.MyCd;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MyCdListResponse {
  private final List<MyCdResponse> data;
  private final Long nextCursor;  // 다음 페이지의 커서 (무한 스크롤)

  // ✅ 생성자는 private → 외부에서 직접 생성 불가
  public MyCdListResponse(List<MyCdResponse> data, Long nextCursor) {
    this.data = data;
    this.nextCursor = nextCursor;
  }

  // ✅ 정적 팩토리 메서드 추가
  public static MyCdListResponse fromEntities(List<MyCd> myCds) {
    List<MyCdResponse> responses = myCds.stream()
        .map(MyCdResponse::fromEntity)
        .collect(Collectors.toList());

    Long nextCursor = myCds.isEmpty() ? null : myCds.get(myCds.size() - 1).getId();

    return new MyCdListResponse(responses, nextCursor);
  }
}
