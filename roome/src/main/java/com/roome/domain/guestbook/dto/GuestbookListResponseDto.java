package com.roome.domain.guestbook.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GuestbookListResponseDto {
    private Long roomId;
    private List<GuestbookResponseDto> guestbook;
    private PaginationDto pagination;
}
