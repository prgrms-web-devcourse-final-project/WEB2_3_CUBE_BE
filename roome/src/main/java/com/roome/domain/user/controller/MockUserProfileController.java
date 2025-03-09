package com.roome.domain.user.controller;

import com.roome.domain.auth.security.OAuth2UserPrincipal;
import com.roome.domain.user.dto.RecommendedUserDto;
import com.roome.domain.user.dto.request.UpdateProfileRequest;
import com.roome.domain.user.dto.response.ImageUploadResponseDto;
import com.roome.domain.user.dto.response.UserProfileResponse;
import com.roome.global.exception.ControllerException;
import com.roome.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Hidden
@Slf4j
@RestController
@RequestMapping("/mock/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Profile", description = "프로필 조회 및 수정")
public class MockUserProfileController {

  private static final Long MOCK_USER_ID = 999L;
  // 모의 버킷 이름
  private static final String MOCK_BUCKET_NAME = "roome-profile-images";
  // 허용할 이미지 확장자 목록
  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
  // 최대 파일 크기 (5MB)
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

  @Operation(summary = "프로필 조회")
  @GetMapping("/{userId}")
  public ResponseEntity<UserProfileResponse> getMockUserProfile(@PathVariable Long userId) {
    log.info("[Mock 프로필 조회] 사용자 ID: {}", userId);

    UserProfileResponse mockResponse = UserProfileResponse
        .builder()
        .id(userId.toString())
        .nickname("Mock User " + userId)
        .profileImage(
            "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
        .bio("안녕하세요. Mock입니다..")
        .bookGenres(List.of("NOVEL", "ESSAY"))
        .musicGenres(List.of("POP", "ROCK"))
        .recommendedUsers(generateMockSimilarUsers())
        .isMyProfile(true)
        .build();

    return ResponseEntity.ok(mockResponse);
  }

  @Operation(summary = "프로필 수정")
  @PatchMapping("/profile")
  public ResponseEntity<Map<String, String>> updateMockUserProfile(
      @AuthenticationPrincipal OAuth2UserPrincipal principal,
      @RequestBody UpdateProfileRequest request) {
    log.info("[Mock 프로필 수정] nickname: {}, bio: {}", request.getNickname(), request.getBio());

    return ResponseEntity.ok(
        Map.of("id", MOCK_USER_ID.toString(), "nickname", request.getNickname(), "bio",
            request.getBio()));
  }

  @Operation(summary = "프로필 이미지 업로드",
      description = "사용자의 프로필 이미지를 업로드합니다. PUT과 POST 모두 지원합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
      @ApiResponse(responseCode = "400", description = "이미지 파일이 없거나 비어 있음 (IMAGE_NOT_FOUND)"),
      @ApiResponse(responseCode = "400", description = "지원되지 않는 이미지 형식 (INVALID_IMAGE_FORMAT)"),
      @ApiResponse(responseCode = "400", description = "이미지 크기가 제한(5MB)을 초과함 (IMAGE_SIZE_EXCEEDED)"),
      @ApiResponse(responseCode = "500", description = "이미지 업로드 중 오류 발생 (S3_UPLOAD_ERROR)")
  })
  @RequestMapping(value = "/profile/image", method = {RequestMethod.POST, RequestMethod.PUT})
  public ResponseEntity<ImageUploadResponseDto> uploadMockProfileImage(
      @AuthenticationPrincipal OAuth2UserPrincipal principal,
      @Parameter(description = "업로드할 프로필 이미지 파일 (JPG, JPEG, PNG, GIF 형식, 최대 5MB)")
      @RequestParam("image") MultipartFile image) {

    // 이미지 파일 유효성 검증
    validateImageFile(image);

    log.info("[Mock 프로필 이미지 업로드] 파일명: {}, 크기: {}", image.getOriginalFilename(), image.getSize());

    // 모의 S3 URL 생성 (실제 업로드는 이루어지지 않음)
    String mockUuid = UUID
        .randomUUID()
        .toString()
        .replace("-", "");
    String mockFileExtension = getFileExtension(image.getOriginalFilename());
    String mockImageUrl =
        "https://" + MOCK_BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/profile/" + mockUuid + "."
            + mockFileExtension;

    ImageUploadResponseDto response = ImageUploadResponseDto
        .builder()
        .imageUrl(mockImageUrl)
        .fileName(image.getOriginalFilename())
        .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "프로필 이미지 삭제",
      description = "사용자의 프로필 이미지를 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 이미지 URL (INVALID_IMAGE_URL)"),
      @ApiResponse(responseCode = "500", description = "이미지 삭제 중 오류 발생 (S3_DELETE_ERROR)")
  })
  @DeleteMapping("/profile/image")
  public ResponseEntity<?> deleteMockProfileImage(
      @AuthenticationPrincipal OAuth2UserPrincipal principal,
      @Parameter(description = "삭제할 이미지의 S3 URL", example = "https://bucket-name.s3.amazonaws.com/profile/image.jpg")
      @RequestParam("imageUrl") String imageUrl) {

    // URL 유효성 검증
    if (!isValidImageUrl(imageUrl)) {
      throw new ControllerException(ErrorCode.INVALID_IMAGE_URL);
    }

    log.info("[Mock 프로필 이미지 삭제] URL: {}", imageUrl);

    return ResponseEntity.ok(imageUrl);
  }

  private List<RecommendedUserDto> generateMockSimilarUsers() {
    return List.of(
        RecommendedUserDto.builder().userId(1L).nickname("Mock User 1").profileImage(
                "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
            .build(),
        RecommendedUserDto.builder().userId(2L).nickname("Mock User 2").profileImage(
                "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
            .build(),
        RecommendedUserDto.builder().userId(3L).nickname("Mock User 3").profileImage(
                "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
            .build(),
        RecommendedUserDto.builder().userId(4L).nickname("Mock User 4").profileImage(
                "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
            .build(),
        RecommendedUserDto.builder().userId(5L).nickname("Mock User 5").profileImage(
                "https://github.com/user-attachments/assets/912a4bc2-da94-4551-8547-b8a47c6e6813")
            .build()
    );
  }

  // 파일 확장자 추출 메서드
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf(".");
    if (lastDotIndex > 0) {
      return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    return "jpg"; // 기본값
  }

  // 이미지 URL 유효성 검증 메서드
  private boolean isValidImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.trim().isEmpty()) {
      return false;
    }

    // URL에 버킷 이름이 포함되어 있는지 확인
    boolean containsBucketName = imageUrl.contains(MOCK_BUCKET_NAME);

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
      String extension = getFileExtension(originalFilename);
      if (!ALLOWED_EXTENSIONS.contains(extension)) {
        throw new ControllerException(ErrorCode.INVALID_IMAGE_FORMAT);
      }
    }
  }
}