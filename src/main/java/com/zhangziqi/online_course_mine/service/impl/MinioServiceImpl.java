package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.config.MinioConfig;
import com.zhangziqi.online_course_mine.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public String uploadFile(String objectName, InputStream inputStream, String contentType) {
        try {
            checkAndCreateBucket();
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
            
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            // 返回直接访问URL，而不是预签名URL
            return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取文件URL失败", e);
        }
    }

    @Override
    public List<String> listAllFiles() {
        List<String> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            
            for (Result<Item> result : results) {
                Item item = result.get();
                files.add(item.objectName());
            }
            return files;
        } catch (Exception e) {
            log.error("列出所有文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("列出所有文件失败", e);
        }
    }

    @Override
    public void checkAndCreateBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
                log.info("Bucket '{}' 创建成功", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("检查或创建存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("检查或创建存储桶失败", e);
        }
    }
} 