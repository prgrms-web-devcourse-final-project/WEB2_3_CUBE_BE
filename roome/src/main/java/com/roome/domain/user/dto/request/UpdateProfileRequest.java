package com.roome.domain.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class UpdateProfileRequest {
    private String nickname;

    @Size(max = 101, message = "자기소개는 100자를 초과할 수 없습니다.")
    private String bio;
}