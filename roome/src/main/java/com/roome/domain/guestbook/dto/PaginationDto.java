package com.roome.domain.guestbook.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationDto {
    private int page;
    private int size;
    private int totalPages;
}
