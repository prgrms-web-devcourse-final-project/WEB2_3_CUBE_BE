package com.roome.domain.user.controller;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.dto.response.ImageUploadResponseDto;
import com.roome.domain.user.service.UserProfileImageService;
import com.roome.domain.user.service.UserService;
import com.roome.global.exception.ControllerException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users/image")
public class UserProfileImageController {
    // 허용할 이미지 확장자 목록
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserProfileImageService userService;


    // 프로필 이미지 업로드
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<ImageUploadResponseDto> uploadProfileImage(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestParam("image") MultipartFile image) {
        // 이미지 파일 유효성 검증
        validateImageFile(image);

        try {
            // S3에 이미지 업로드된 기존 이미지 저장. 없으면 null
            String originProfileImageUrl = userService.getProfileImageUrl(principal.getId());
            // 새로운 프로필 이미지 업로드
            String imageUrl = s3Service.uploadImage(image, "profile");
            userService.updateProfileImage(principal.getId(), imageUrl);
            log.info("프로필 이미지 업로드 완료: {}", imageUrl);
            //기존 프로필 이미지 삭제
            if (originProfileImageUrl != null && !originProfileImageUrl.isEmpty()) {
                s3Service.deleteImage(originProfileImageUrl);
                log.info("기존 프로필 이미지 삭제 완료: {}", originProfileImageUrl);
            }
            // 응답 데이터 구성
            ImageUploadResponseDto response = ImageUploadResponseDto
                    .builder()
                    .imageUrl(imageUrl)
                    .fileName(image.getOriginalFilename())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new ControllerException(ErrorCode.S3_UPLOAD_ERROR);
        }
    }

    // 프로필 이미지 삭제
    @DeleteMapping
    public ResponseEntity<?> deleteProfileImage(@AuthenticationPrincipal OAuth2UserPrincipal principal, @RequestParam("imageUrl") String imageUrl) {
        // URL 유효성 검증
        if (imageUrl == null || imageUrl
                .trim()
                .isEmpty() || !isValidImageUrl(imageUrl)) {
            throw new ControllerException(ErrorCode.INVALID_IMAGE_URL);
        }

        try {
            s3Service.deleteImage(imageUrl);
            userService.deleteProfileImage(principal.getId());

            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            throw new ControllerException(ErrorCode.S3_DELETE_ERROR);
        }
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    // 이미지 URL 유효성 검증 메서드
    private boolean isValidImageUrl(String imageUrl) {
        String bucketName = s3Service.getBucketName();

        if (imageUrl == null || imageUrl
                .trim()
                .isEmpty()) {
            return false;
        }

        // AWS S3 버킷 도메인 확인
        String bucketDomain = bucketName + ".s3.amazonaws.com";

        // URL에 버킷 이름이 포함되어 있는지 확인
        boolean containsBucketName = imageUrl.contains(bucketName);

        // URL이 amazonaws.com 도메인을 포함하는지 확인
        boolean isAwsUrl = imageUrl.contains("amazonaws.com");

        // URL이 /profile/ 디렉토리를 참조하는지 확인 (프로필 이미지 경로)
        boolean isProfileDirectory = imageUrl.contains("/profile/");

        // UUID 패턴을 포함하는지 대략적으로 확인 (UUID 길이는 32자)
        boolean containsUuidPattern = imageUrl.matches(".*[a-f0-9]{32}.*\\.(jpg|jpeg|png|gif)");

        return isAwsUrl && containsBucketName && isProfileDirectory && containsUuidPattern;
    }

    // 이미지 파일 유효성 검증
    public void validateImageFile(MultipartFile image) {
        // 파일 존재 여부 확인
        if (image == null || image.isEmpty()) {
            throw new ControllerException(ErrorCode.IMAGE_NOT_FOUND);
        }

        // 파일 크기 확인
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new ControllerException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }

        // 파일 형식 검증
        String originalFilename = image.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ControllerException(ErrorCode.INVALID_IMAGE_FORMAT);
            }
        }
    }

}