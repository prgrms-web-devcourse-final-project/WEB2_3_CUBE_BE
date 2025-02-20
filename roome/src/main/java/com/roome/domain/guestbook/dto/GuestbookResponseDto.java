package com.roome.domain.guestbook.dto;

import com.roome.domain.guestbook.entity.Guestbook;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GuestbookResponseDto {
    private Long guestbookId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String message;
    private LocalDateTime createdAt;
    private String relation;

    public static GuestbookResponseDto from(Guestbook guestbook) {
        return GuestbookResponseDto.builder()
                .guestbookId(guestbook.getGuestbookId())
                .userId(guestbook.getUser().getId())
                .nickname(guestbook.getNickname())
                .profileImage(guestbook.getProfileImage())
                .message(guestbook.getMessage())
                .createdAt(guestbook.getCreatedAt())
                .relation(guestbook.getRelation().name())
                .build();
    }
}
