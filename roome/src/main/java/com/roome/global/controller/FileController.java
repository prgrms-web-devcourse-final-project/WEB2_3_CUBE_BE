//package com.roome.global.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/v1/files")
//@RequiredArgsConstructor
//public class FileController {
//
//    private final S3Service s3Service;
//
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
//        try {
//            String fileUrl = s3Service.uploadFile(file);
//            return ResponseEntity.ok(new FileResponse(fileUrl, "파일 업로드에 성공했습니다."));
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new FileResponse(null, "파일 업로드에 실패했습니다: " + e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{fileName}")
//    public ResponseEntity<FileResponse> deleteFile(@PathVariable String fileName) {
//        try {
//            s3Service.deleteFile(fileName);
//            return ResponseEntity.ok(new FileResponse(null, "파일이 성공적으로 삭제되었습니다."));
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new FileResponse(null, "파일 삭제에 실패했습니다: " + e.getMessage()));
//        }
//    }
//}
