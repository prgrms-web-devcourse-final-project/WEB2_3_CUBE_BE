//package com.roome.global.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class S3Service {
//
//    private final S3Client s3Client;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    public String uploadFile(MultipartFile file) {
//        String fileName = createFileName(file.getOriginalFilename());
//
//        try {
//            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                                                                .bucket(bucket)
//                                                                .key(fileName)
//                                                                .contentType(file.getContentType())
//                                                                .build();
//
//            s3Client.putObject(putObjectRequest,
//                               RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//
//            return String.format("https://%s.s3.%s.amazonaws.com/%s",
//                                 bucket, s3Client.serviceClientConfiguration().region(), fileName);
//
//        } catch (IOException e) {
//            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
//        }
//    }
//
//    private String createFileName(String originalFileName) {
//        return UUID.randomUUID().toString() + "_" + originalFileName;
//    }
//
//    public void deleteFile(String fileName) {
//        s3Client.deleteObject(builder -> builder
//                .bucket(bucket)
//                .key(fileName)
//                .build());
//    }
//}