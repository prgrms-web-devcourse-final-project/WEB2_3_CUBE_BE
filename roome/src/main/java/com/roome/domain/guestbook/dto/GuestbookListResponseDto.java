package com.roome.domain.guestbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GuestbookListResponseDto {
    private Long roomId;
    private List<GuestbookResponseDto> guestbook;
    private PaginationDto pagination;
}
