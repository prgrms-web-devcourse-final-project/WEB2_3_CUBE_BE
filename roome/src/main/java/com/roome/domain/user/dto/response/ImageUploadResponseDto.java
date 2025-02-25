package com.roome.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImageUploadResponseDto {
    private String imageUrl;
    private String fileName;
}
