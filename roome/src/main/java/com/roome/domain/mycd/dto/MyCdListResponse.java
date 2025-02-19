package com.roome.domain.mycd.dto;

import com.roome.domain.mycd.entity.MyCd;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class MyCdListResponse {
  private List<MyCdResponse> data;

  public static MyCdListResponse fromEntities(List<MyCd> myCds) {
    List<MyCdResponse> responses = myCds.stream()
        .map(MyCdResponse::fromEntity)
        .collect(Collectors.toList());
    return new MyCdListResponse(responses);
  }
}
