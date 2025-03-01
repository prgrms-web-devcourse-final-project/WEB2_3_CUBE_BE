package com.roome.domain.houseMate.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class HousemateResponseDto {
    private Long id;           // AddedHousemate의 ID
    private Long userId;       // 하우스메이트를 추가한 사용자 ID
    private Long addedId;      // 하우스메이트로 추가된 사용자 ID
    private LocalDateTime createdAt;  // 생성 시간

    // 추가적으로 필요한 정보 (예: 추가된 사용자의 이름, 프로필 이미지 URL 등)
    private String addedUserName;
    private String addedUserProfileImageUrl;
}
