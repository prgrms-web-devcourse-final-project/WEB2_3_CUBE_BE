package com.roome.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MockUpdateProfileRequest {
    private String nickname;
    private String profileImage;
    private String bio;
}
