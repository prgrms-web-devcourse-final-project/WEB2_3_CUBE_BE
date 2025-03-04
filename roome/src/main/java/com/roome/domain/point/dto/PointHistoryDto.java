package com.roome.domain.point.dto;

import com.roome.domain.point.entity.PointHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class PointHistoryDto {

  private Long id;
  private String dateTime;
  private String type;
  private String reason;
  private String amount;

  public static PointHistoryDto fromEntity(PointHistory history) {
    return new PointHistoryDto(
        history.getId(),
        history.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        history.getAmount() > 0 ? "적립" : "차감",
        history.getReason().name(),
        (history.getAmount() > 0 ? "+" : "") + history.getAmount() + "P"
    );
  }
}
