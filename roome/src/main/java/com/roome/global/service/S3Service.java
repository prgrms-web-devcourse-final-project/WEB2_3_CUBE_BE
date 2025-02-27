package com.roome.global.service;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;

    @Getter
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // S3에 이미지 업로드
    // MultipartFile: 업로드할 파일
    // dirName: S3 버킷 내 디렉토리 이름
    // return: 업로드된 이미지 URL
    public String uploadImage(MultipartFile file, String dirName) throws IOException {
        // 파일 이름 생성 (UUID 사용하여 고유한 이름 생성)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFileName = UUID.randomUUID().toString().replaceAll("-", "") + extension;

        // S3 키 생성 (디렉토리/파일명)
        String key = dirName + "/" + storedFileName;

        // 메타데이터 설정
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                            .bucket(bucketName)
                                                            .key(key)
                                                            .contentType(file.getContentType())
                                                            .build();
        // 파일 업로드
        s3Client.putObject(putObjectRequest,
                           RequestBody.fromBytes(file.getBytes()));
        log.info("파일 업로드 완료: {}", key);
        // 업로드된 파일의 URL 반환
        return getFileUrl(key);
    }

    // S3에 이미지 삭제
    // fileUrl: 삭제할 이미지 URL
    public void deleteImage(String fileUrl) {
        String key = fileUrl;

        // URL에서 키 추출 (URL인 경우)
        if (fileUrl.startsWith("http")) {
            try {
                // URL을 객체로 파싱
                java.net.URL url = new java.net.URL(fileUrl);
                // URL 경로에서 첫 번째 슬래시 제거
                key = url.getPath();
                if (key.startsWith("/")) {
                    key = key.substring(1);
                }
            } catch (Exception e) {
                // URL 파싱 실패 시 예외 발생
                throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
            }
        } else {
            key = fileUrl;
        }

        // 파일 삭제
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                                                                     .bucket(bucketName)
                                                                     .key(key)
                                                                     .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    // S3에 업로드된 파일 URL 반환
    // key: S3 키
    private String getFileUrl(String key) {
        GetUrlRequest request = GetUrlRequest.builder()
                                             .bucket(bucketName)
                                             .key(key)
                                             .build();

        URL url = s3Client.utilities().getUrl(request);
        return url.toString();
    }

}