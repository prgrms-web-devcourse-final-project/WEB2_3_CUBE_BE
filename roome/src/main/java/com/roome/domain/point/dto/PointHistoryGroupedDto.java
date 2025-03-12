package com.roome.domain.point.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointHistoryGroupedDto {

  private String date;
  private List<PointHistoryDto> items;  // 해당 날짜의 포인트 내역 목록
}
