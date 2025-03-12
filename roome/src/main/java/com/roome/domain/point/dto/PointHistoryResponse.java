package com.roome.domain.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PointHistoryResponse {

  private List<PointHistoryGroupedDto> history;  // 날짜별 그룹핑된 데이터
  private int balance;
  private long totalCount;
  private Long firstId;
  private Long lastId;
  private String nextDayCursor;  // 날짜 기준 커서
  private Long nextItemCursor;   // 개별 항목 기준 커서

  public static PointHistoryResponse fromEntityList(List<PointHistoryGroupedDto> historyList,
      int balance,
      long totalCount, Long firstId, Long lastId, String nextDayCursor, Long nextItemCursor) {
    return new PointHistoryResponse(
        historyList, balance, totalCount, firstId, lastId, nextDayCursor, nextItemCursor
    );
  }
}
