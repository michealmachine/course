package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.config.MinioConfig;
import com.zhangziqi.online_course_mine.service.impl.MinioServiceImpl;
import io.minio.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfig minioConfig;

    private MinioService minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioServiceImpl(minioClient, minioConfig);
        when(minioConfig.getBucketName()).thenReturn("media-test");
    }

    @Test
    void uploadFile_Success() throws Exception {
        // 准备测试数据
        String objectName = "test-file.txt";
        String content = "Hello MinIO!";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        String contentType = "text/plain";
        
        // 模拟MinIO客户端行为
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        
        // 模拟配置
        String endpoint = "http://localhost:8999";
        String bucketName = "media-test";
        when(minioConfig.getEndpoint()).thenReturn(endpoint);
        when(minioConfig.getBucketName()).thenReturn(bucketName);
        
        // 预期的永久URL
        String expectedUrl = endpoint + "/" + bucketName + "/" + objectName;
        
        // 执行测试
        String url = minioService.uploadFile(objectName, inputStream, contentType);
        
        // 验证结果
        assertNotNull(url);
        assertEquals(expectedUrl, url);
        
        // 验证MinIO客户端的方法被调用
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
        // 不再验证getPresignedObjectUrl，因为我们不再使用它
    }

    @Test
    void deleteFile_Success() throws Exception {
        // 准备测试数据
        String objectName = "test-file.txt";
        
        // 执行测试
        boolean result = minioService.deleteFile(objectName);
        
        // 验证结果
        assertTrue(result);
        
        // 验证MinIO客户端的方法被调用
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void listAllFiles_Success() throws Exception {
        // 准备测试数据
        List<String> expectedFiles = List.of("file1.txt", "file2.txt");
        
        // 创建模拟的Result<Item>
        List<Result<Item>> mockResults = new ArrayList<>();
        for (String fileName : expectedFiles) {
            Result<Item> mockResult = mock(Result.class);
            Item mockItem = mock(Item.class);
            when(mockItem.objectName()).thenReturn(fileName);
            when(mockResult.get()).thenReturn(mockItem);
            mockResults.add(mockResult);
        }
        
        // 创建一个可迭代的对象
        Iterable<Result<Item>> mockIterable = () -> mockResults.iterator();
        
        // 模拟MinIO客户端行为
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(mockIterable);
        
        // 执行测试
        List<String> files = minioService.listAllFiles();
        
        // 验证结果
        assertNotNull(files);
        assertEquals(expectedFiles.size(), files.size());
        assertTrue(files.containsAll(expectedFiles));
        
        // 验证MinIO客户端的方法被调用
        verify(minioClient).listObjects(any(ListObjectsArgs.class));
    }

    @Test
    void checkAndCreateBucket_BucketExists() throws Exception {
        // 模拟MinIO客户端行为
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        
        // 执行测试
        minioService.checkAndCreateBucket();
        
        // 验证MinIO客户端的方法被调用
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void checkAndCreateBucket_BucketDoesNotExist() throws Exception {
        // 模拟MinIO客户端行为
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        
        // 执行测试
        minioService.checkAndCreateBucket();
        
        // 验证MinIO客户端的方法被调用
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void getFileUrl_Success() {
        // 准备测试数据
        String objectName = "test-file.txt";
        String endpoint = "http://localhost:8999";
        String bucketName = "media-test";
        
        // 模拟配置
        when(minioConfig.getEndpoint()).thenReturn(endpoint);
        when(minioConfig.getBucketName()).thenReturn(bucketName);
        
        // 预期的永久URL
        String expectedUrl = endpoint + "/" + bucketName + "/" + objectName;
        
        // 执行测试
        String url = minioService.getFileUrl(objectName);
        
        // 验证结果
        assertNotNull(url);
        assertEquals(expectedUrl, url);
    }
} 