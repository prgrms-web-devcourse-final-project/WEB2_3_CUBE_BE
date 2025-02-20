package com.roome.domain.guestbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaginationDto {
    private int page;
    private int size;
    private int totalPages;
}
