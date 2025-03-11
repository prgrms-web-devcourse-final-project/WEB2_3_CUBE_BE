package com.roome.domain.user.controller;

import com.roome.domain.user.dto.response.ImageUploadResponseDto;
import com.roome.domain.user.service.UserProfileImageService;
import com.roome.global.auth.AuthenticatedUser;
import com.roome.global.exception.ControllerException;
import com.roome.global.exception.ErrorCode;
import com.roome.global.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "사용자 프로필 이미지 API", description = "사용자 프로필 이미지 관리를 위한 API")
@Slf4j
@Validated
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


  @Operation(summary = "프로필 이미지 업로드",
      description = "사용자의 프로필 이미지를 업로드합니다. PUT과 POST 모두 지원합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
      @ApiResponse(responseCode = "400", description = "이미지 파일이 없거나 비어 있음 (IMAGE_NOT_FOUND)"),
      @ApiResponse(responseCode = "400", description = "지원되지 않는 이미지 형식 (INVALID_IMAGE_FORMAT)"),
      @ApiResponse(responseCode = "400", description = "이미지 크기가 제한(5MB)을 초과함 (IMAGE_SIZE_EXCEEDED)"),
      @ApiResponse(responseCode = "500", description = "이미지 업로드 중 오류 발생 (S3_UPLOAD_ERROR)")
  })
  @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
  public ResponseEntity<ImageUploadResponseDto> uploadProfileImage(
      @AuthenticatedUser Long userId,
      @Parameter(description = "업로드할 프로필 이미지 파일 (JPG, JPEG, PNG, GIF 형식, 최대 5MB)")
      @RequestParam("image") MultipartFile image) {
    // 이미지 파일 유효성 검증
    validateImageFile(image);

    try {
      // S3에 이미지 업로드된 기존 이미지 저장. 없으면 null
      String originProfileImageUrl = userService.getProfileImageUrl(userId);
      // 새로운 프로필 이미지 업로드
      String imageUrl = s3Service.uploadImage(image, "profile");
      userService.updateProfileImage(userId, imageUrl);
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

  @Operation(summary = "프로필 이미지 삭제",
      description = "사용자의 프로필 이미지를 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 이미지 URL (INVALID_IMAGE_URL)"),
      @ApiResponse(responseCode = "500", description = "이미지 삭제 중 오류 발생 (S3_DELETE_ERROR)")
  })
  @DeleteMapping
  public ResponseEntity<?> deleteProfileImage(
      @AuthenticatedUser Long userId,
      @Parameter(description = "삭제할 이미지의 S3 URL", example = "https://bucket-name.s3.amazonaws.com/profile/image.jpg")
      @RequestParam("imageUrl") String imageUrl) {
    // URL 유효성 검증
    if (imageUrl == null || imageUrl
        .trim()
        .isEmpty() || !isValidImageUrl(imageUrl)) {
      throw new ControllerException(ErrorCode.INVALID_IMAGE_URL);
    }

    try {
      s3Service.deleteImage(imageUrl);
      userService.deleteProfileImage(userId);

      return ResponseEntity.ok(imageUrl);
    } catch (Exception e) {
      throw new ControllerException(ErrorCode.S3_DELETE_ERROR);
    }
  }

  // 파일 확장자 추출 메서드
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf(".");
    if (lastDotIndex >= 0) {  // > 0에서 >= 0으로 변경하여 점으로 시작하는 파일명도 처리
      return filename.substring(lastDotIndex + 1);
    }
    return "";
  }

  // 이미지 URL 유효성 검증 메서드
  private boolean isValidImageUrl(String imageUrl) {
    String bucketName = s3Service.getBucketName();

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