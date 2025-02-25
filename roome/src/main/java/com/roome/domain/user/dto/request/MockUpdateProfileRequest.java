package com.roome.domain.user.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MockUpdateProfileRequest {
    private String nickname;
    private String bio;
}
