package com.roome.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
//    @NotBlank(message = "닉네임은 필수입니다.")
//    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자의 한글, 영문, 숫자만 가능합니다.")
    private String nickname;

    @Size(max = 30, message = "자기소개는 30자를 초과할 수 없습니다.")
    private String bio;
}