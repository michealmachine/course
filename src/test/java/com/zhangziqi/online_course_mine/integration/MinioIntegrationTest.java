package com.zhangziqi.online_course_mine.integration;

import com.zhangziqi.online_course_mine.service.MinioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
class MinioIntegrationTest {

    @Autowired
    private MinioService minioService;

    /**
     * 这个测试只有在MINIO_ENABLED环境变量为true时才会运行
     * 可以在运行测试前设置环境变量：MINIO_ENABLED=true
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "MINIO_ENABLED", matches = "true")
    void testMinioOperations() throws Exception {
        // 要测试的文件数据
        String objectName = "test-integration-" + System.currentTimeMillis() + ".txt";
        String content = "Hello MinIO Integration Test!";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        String contentType = "text/plain";

        try {
            // 1. 上传文件
            String url = minioService.uploadFile(objectName, inputStream, contentType);
            assertNotNull(url);
            // 永久URL应该包含对象名
            assertTrue(url.contains(objectName));
            
            // 2. 获取文件URL
            String fileUrl = minioService.getFileUrl(objectName);
            assertNotNull(fileUrl);
            // 两个URL应该完全相同，因为都是用相同方式构建的永久URL
            assertEquals(url, fileUrl);
            
            // 3. 列出所有文件
            List<String> files = minioService.listAllFiles();
            assertNotNull(files);
            assertTrue(files.contains(objectName));
            
        } finally {
            // 4. 删除测试文件（清理）
            boolean deleted = minioService.deleteFile(objectName);
            assertTrue(deleted);
            
            // 验证文件已被删除
            List<String> filesAfterDelete = minioService.listAllFiles();
            assertFalse(filesAfterDelete.contains(objectName));
        }
    }
    
    /**
     * 一个总是跳过的测试，用于演示如何运行集成测试
     */
    @Test
    void testSkippedByDefault() {
        // 默认跳过这个测试
        assumeTrue(false, "手动跳过的测试，需要正确配置MinIO才能运行");
        fail("这个测试不应该被执行");
    }
} 