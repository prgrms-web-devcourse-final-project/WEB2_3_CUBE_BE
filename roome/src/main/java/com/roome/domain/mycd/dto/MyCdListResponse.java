package com.roome.domain.mycd.dto;

import com.roome.domain.mycd.entity.MyCd;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // 기본 생성자 추가 (역직렬화 오류 해결)
public class MyCdListResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<MyCdResponse> data;
  private Long nextCursor;  // 다음 페이지의 커서 (무한 스크롤)
  private long totalCount;  // 전체 개수 추가
  private Long firstMyCdId;
  private Long lastMyCdId;

  public MyCdListResponse(List<MyCdResponse> data, Long nextCursor, long totalCount,
      Long firstMyCdId, Long lastMyCdId) {
    this.data = data;
    this.nextCursor = nextCursor;
    this.totalCount = totalCount;
    this.firstMyCdId = firstMyCdId;
    this.lastMyCdId = lastMyCdId;
  }

  public static MyCdListResponse fromEntities(List<MyCd> myCds, long totalCount, long firstMyCdId,
      long lastMyCdId) {
    List<MyCdResponse> responses = myCds.stream()
        .map(MyCdResponse::fromEntity)
        .collect(Collectors.toList());

    Long nextCursor = myCds.isEmpty() ? null : myCds.get(myCds.size() - 1).getId();

    return new MyCdListResponse(responses, nextCursor, totalCount, firstMyCdId, lastMyCdId);
  }
}
