package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.config.S3Config;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.media.PresignedUrlInfo;
import com.zhangziqi.online_course_mine.model.dto.media.UploadStatusInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * S3分片上传管理器
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3MultipartUploadManager {
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Config s3Config;
    
    /**
     * 初始化分片上传
     * 
     * @param objectKey 对象键
     * @param contentType 内容类型
     * @return 上传ID
     */
    public String initiateMultipartUpload(String objectKey, String contentType) {
        try {
            // 确保存储桶存在
            checkAndCreateBucket();
            
            // 初始化分片上传请求
            CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            
            // 发送请求
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createRequest);
            
            log.info("已初始化分片上传: uploadId={}, objectKey={}", response.uploadId(), objectKey);
            return response.uploadId();
        } catch (Exception e) {
            log.error("初始化分片上传失败", e);
            throw new BusinessException(500, "初始化上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 为分片生成预签名上传URL
     * 
     * @param uploadId 上传ID
     * @param objectKey 对象键
     * @param partNumber 分片编号
     * @return 预签名URL
     */
    public String generatePresignedUrlForPart(String uploadId, String objectKey, int partNumber) {
        try {
            // 构建上传分片请求
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();
            
            // 构建预签名请求
            software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest presignRequest = 
                    software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .uploadPartRequest(uploadPartRequest)
                    .build();
            
            // 获取预签名URL
            software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest presignedRequest = 
                    s3Presigner.presignUploadPart(presignRequest);
            
            String url = presignedRequest.url().toString();
            log.debug("生成分片预签名URL: part={}, uploadId={}", partNumber, uploadId);
            
            return url;
        } catch (Exception e) {
            log.error("生成分片预签名URL失败", e);
            throw new BusinessException(500, "生成分片上传URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量生成分片预签名URL
     * 
     * @param uploadId 上传ID
     * @param objectKey 对象键
     * @param partStart 起始分片编号
     * @param partEnd 结束分片编号
     * @return 预签名URL信息列表
     */
    public List<PresignedUrlInfo> batchGeneratePresignedUrls(
            String uploadId, String objectKey, int partStart, int partEnd) {
        List<PresignedUrlInfo> urlInfos = new ArrayList<>();
        
        for (int i = partStart; i <= partEnd; i++) {
            String url = generatePresignedUrlForPart(uploadId, objectKey, i);
            urlInfos.add(new PresignedUrlInfo(i, url));
        }
        
        return urlInfos;
    }
    
    /**
     * 完成分片上传
     * 
     * @param uploadId 上传ID
     * @param objectKey 对象键
     * @param parts 已上传的分片信息
     * @return 完成上传响应
     */
    public CompleteMultipartUploadResponse completeMultipartUpload(
            String uploadId, String objectKey, List<UploadStatusInfo.PartInfo> parts) {
        try {
            log.info("开始完成分片上传 - uploadId: {}, objectKey: {}, 分片数量: {}", uploadId, objectKey, parts.size());
            log.info("原始分片信息: {}", parts);
            
            // 检查是否有分片
            if (parts.isEmpty()) {
                log.error("没有可合并的分片 - uploadId: {}, objectKey: {}", uploadId, objectKey);
                throw new BusinessException(400, "合并失败：没有可合并的分片");
            }
            
            // 将分片信息转换为AWS SDK所需格式
            List<CompletedPart> completedParts = parts.stream()
                    .map(part -> {
                        String eTag = part.getETag();
                        log.info("处理分片 - partNumber: {}, eTag: '{}'", part.getPartNumber(), eTag);
                        
                        // 验证ETag是否有效
                        if (eTag == null || eTag.isEmpty()) {
                            log.error("分片ETag为空 - partNumber: {}", part.getPartNumber());
                            throw new BusinessException(400, 
                                    String.format("分片 %d 的ETag为空，无法完成合并", part.getPartNumber()));
                        }
                        
                        return CompletedPart.builder()
                                .partNumber(part.getPartNumber())
                                .eTag(eTag)
                                .build();
                    })
                    .sorted(Comparator.comparing(CompletedPart::partNumber))
                    .collect(Collectors.toList());
            
            log.info("转换后的AWS SDK分片信息: {}", completedParts);
            
            // 构建完成请求
            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build())
                    .build();
            
            log.info("发送完成分片上传请求: {}", completeRequest);
            
            // 完成上传
            CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(completeRequest);
            log.info("分片上传完成: uploadId={}, objectKey={}, location={}, eTag={}", 
                    uploadId, objectKey, response.location(), response.eTag());
            
            return response;
        } catch (Exception e) {
            log.error("完成分片上传失败 - uploadId: {}, objectKey: {}, 错误: {}", uploadId, objectKey, e.getMessage(), e);
            throw new BusinessException(500, "完成上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消分片上传
     * 
     * @param uploadId 上传ID
     * @param objectKey 对象键
     */
    public void abortMultipartUpload(String uploadId, String objectKey) {
        try {
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build();
            
            s3Client.abortMultipartUpload(abortRequest);
            log.info("已取消分片上传: uploadId={}, objectKey={}", uploadId, objectKey);
        } catch (Exception e) {
            log.error("取消分片上传失败", e);
            throw new BusinessException(500, "取消上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取上传分片状态
     * 
     * @param uploadId 上传ID
     * @param objectKey 对象键
     * @return 已上传的分片信息
     */
    public List<UploadStatusInfo.PartInfo> listParts(String uploadId, String objectKey) {
        try {
            ListPartsRequest listPartsRequest = ListPartsRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build();
            
            ListPartsResponse response = s3Client.listParts(listPartsRequest);
            
            return response.parts().stream()
                    .map(part -> new UploadStatusInfo.PartInfo(
                            part.partNumber(), 
                            part.eTag()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取分片信息失败", e);
            throw new BusinessException(500, "获取上传状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查并创建存储桶
     */
    private void checkAndCreateBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .build());
        } catch (NoSuchBucketException e) {
            log.info("存储桶 {} 不存在，正在创建...", s3Config.getBucketName());
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .build());
            log.info("存储桶 {} 创建成功", s3Config.getBucketName());
        }
    }
    
    /**
     * 获取对象的预签名访问URL
     * 
     * @param objectKey 对象键
     * @param expirationMinutes URL有效期（分钟）
     * @return 预签名URL
     */
    public String generatePresignedGetUrl(String objectKey, long expirationMinutes) {
        try {
            // 构建请求
            software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest presignRequest = 
                    software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(s3Config.getBucketName())
                            .key(objectKey)
                            .build())
                    .build();
            
            // 获取预签名URL
            software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presignedRequest = 
                    s3Presigner.presignGetObject(presignRequest);
            
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("生成预签名访问URL失败", e);
            throw new BusinessException(500, "生成访问链接失败: " + e.getMessage());
        }
    }
} 