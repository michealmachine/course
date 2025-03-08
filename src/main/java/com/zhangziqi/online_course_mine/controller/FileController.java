package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID() + extension;
            
            // 上传文件到MinIO
            String fileUrl = minioService.uploadFile(
                    objectName, 
                    file.getInputStream(), 
                    file.getContentType()
            );
            
            // 返回文件URL
            Map<String, String> response = new HashMap<>();
            response.put("fileName", objectName);
            response.put("url", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{objectName}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String objectName) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = minioService.deleteFile(objectName);
        
        if (deleted) {
            response.put("success", true);
            response.put("message", "文件删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "文件删除失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{objectName}")
    public ResponseEntity<Map<String, String>> getFileUrl(@PathVariable String objectName) {
        try {
            String url = minioService.getFileUrl(objectName);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取文件URL失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<String>> listAllFiles() {
        try {
            List<String> files = minioService.listAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
} 