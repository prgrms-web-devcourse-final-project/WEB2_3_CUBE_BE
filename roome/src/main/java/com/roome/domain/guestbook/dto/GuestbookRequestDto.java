package com.roome.domain.guestbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GuestbookRequestDto {
    @NotBlank(message = "방명록 내용은 필수 입력 항목입니다.")
    @Size(max = 1000, message = "방명록 내용은 최대 1000자까지 입력할 수 있습니다.")
    private String message;
}
