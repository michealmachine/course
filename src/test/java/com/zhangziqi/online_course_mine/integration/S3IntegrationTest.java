package com.zhangziqi.online_course_mine.integration;

import com.zhangziqi.online_course_mine.config.S3Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
class S3IntegrationTest {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Autowired
    private S3Config s3Config;

    /**
     * 这个测试只有在S3_ENABLED环境变量为true时才会运行
     * 可以在运行测试前设置环境变量：S3_ENABLED=true
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "S3_ENABLED", matches = "true")
    void testS3Operations() {
        // 要测试的文件数据
        String objectKey = "test-s3-integration-" + System.currentTimeMillis() + ".txt";
        String content = "Hello S3 Integration Test!";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

        try {
            // 1. 检查存储桶是否存在，不存在则创建
            createBucketIfNotExists();

            // 2. 上传文件
            PutObjectResponse putResponse = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Config.getBucketName())
                            .key(objectKey)
                            .contentType("text/plain")
                            .build(),
                    RequestBody.fromBytes(contentBytes)
            );
            assertNotNull(putResponse.eTag());

            // 3. 获取文件的预签名URL
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(s3Config.getBucketName())
                            .key(objectKey)
                            .build())
                    .signatureDuration(Duration.ofDays(7))
                    .build());
            String presignedUrl = presignedRequest.url().toString();
            assertNotNull(presignedUrl);
            assertTrue(presignedUrl.contains(objectKey));

            // 4. 列出所有文件
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(s3Config.getBucketName())
                            .build()
            );
            List<S3Object> objects = listResponse.contents();
            assertNotNull(objects);
            assertTrue(objects.stream().anyMatch(obj -> obj.key().equals(objectKey)));

            // 5. 获取文件元数据
            HeadObjectResponse headResponse = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(s3Config.getBucketName())
                            .key(objectKey)
                            .build()
            );
            assertEquals("text/plain", headResponse.contentType());
            assertEquals(contentBytes.length, headResponse.contentLength());

        } finally {
            // 6. 删除测试文件（清理）
            DeleteObjectResponse deleteResponse = s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(s3Config.getBucketName())
                            .key(objectKey)
                            .build()
            );
            assertNotNull(deleteResponse);

            // 验证文件已被删除
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(s3Config.getBucketName())
                            .build()
            );
            assertFalse(listResponse.contents().stream().anyMatch(obj -> obj.key().equals(objectKey)));
        }
    }

    private void createBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .build());
        }
    }

    /**
     * 一个总是跳过的测试，用于演示如何运行集成测试
     */
    @Test
    void testSkippedByDefault() {
        // 默认跳过这个测试
        assumeTrue(false, "手动跳过的测试，需要正确配置MinIO和S3才能运行");
        fail("这个测试不应该被执行");
    }
} 