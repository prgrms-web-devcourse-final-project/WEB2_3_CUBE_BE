package com.roome.domain.mate.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HousemateListResponse {
    private List<HousemateInfo> housemates;
    private String nextCursor;
    private boolean hasNext;
}