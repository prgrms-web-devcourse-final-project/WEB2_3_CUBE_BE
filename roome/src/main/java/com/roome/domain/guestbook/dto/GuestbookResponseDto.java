package com.roome.domain.guestbook.dto;

import com.roome.domain.guestbook.entity.Guestbook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GuestbookResponseDto {
    private Long guestbookId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private String message;
    private LocalDate createdAt;
    private String relation;

    public static GuestbookResponseDto from(Guestbook guestbook) {
        return GuestbookResponseDto.builder()
                .guestbookId(guestbook.getGuestbookId())
                .userId(guestbook.getUser().getId())
                .nickname(guestbook.getUser().getNickname())
                .profileImage(guestbook.getUser().getProfileImage())
                .message(guestbook.getMessage())
                .createdAt(guestbook.getCreatedAt().toLocalDate())
                .relation(guestbook.getRelation().name())
                .build();
    }

    public static GuestbookResponseDto from(Guestbook guestbook, boolean isHousemate) {
        return GuestbookResponseDto.builder()
                .guestbookId(guestbook.getGuestbookId())
                .userId(guestbook.getUser().getId())
                .nickname(guestbook.getUser().getNickname())
                .profileImage(guestbook.getUser().getProfileImage())
                .message(guestbook.getMessage())
                .createdAt(guestbook.getCreatedAt().toLocalDate()) // YYYY-MM-DD만 반환
                .relation(isHousemate ? "하우스메이트" : guestbook.getRelation().name()) // 하우스메이트 여부 반영
                .build();
    }
}
