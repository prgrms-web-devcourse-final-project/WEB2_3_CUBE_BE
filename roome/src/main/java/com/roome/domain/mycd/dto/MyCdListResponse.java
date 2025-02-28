package com.roome.domain.mycd.dto;

import com.roome.domain.mycd.entity.MyCd;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MyCdListResponse {
  private final List<MyCdResponse> data;
  private final Long nextCursor;  // 다음 페이지의 커서 (무한 스크롤)
  private final long totalCount;  // 전체 개수 추가

  public MyCdListResponse(List<MyCdResponse> data, Long nextCursor, long totalCount) {
    this.data = data;
    this.nextCursor = nextCursor;
    this.totalCount = totalCount;
  }

  public static MyCdListResponse fromEntities(List<MyCd> myCds, long totalCount) {
    List<MyCdResponse> responses = myCds.stream()
        .map(MyCdResponse::fromEntity)
        .collect(Collectors.toList());

    Long nextCursor = myCds.isEmpty() ? null : myCds.get(myCds.size() - 1).getId();

    return new MyCdListResponse(responses, nextCursor, totalCount);
  }
}
