package com.roome.domain.houseMate.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HousemateListResponse {
    private List<HousemateInfo> housemates;
    private Long nextCursor;
    private boolean hasNext;
}