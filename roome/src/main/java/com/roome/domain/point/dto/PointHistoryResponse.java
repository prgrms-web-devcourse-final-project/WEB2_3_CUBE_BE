package com.roome.domain.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PointHistoryResponse {

  private List<PointHistoryDto> history;
  private int balance;
  private long totalCount;
  private Long firstId;
  private Long lastId;
  private Long nextCursor;

  public static PointHistoryResponse fromEntityList(List<PointHistoryDto> historyList, int balance,
      long totalCount, Long firstId, Long lastId, Long nextCursor) {
    return new PointHistoryResponse(
        historyList,
        balance,
        totalCount,
        firstId,
        lastId,
        nextCursor
    );
  }
}
