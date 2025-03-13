package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.media.*;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Media;
import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.MediaRepository;
import com.zhangziqi.online_course_mine.service.MediaService;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import com.zhangziqi.online_course_mine.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 媒体服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    
    private final MediaRepository mediaRepository;
    private final InstitutionRepository institutionRepository;
    private final StorageQuotaService storageQuotaService;
    private final S3MultipartUploadManager s3UploadManager;
    private final UploadStatusService uploadStatusService;
    private final MinioService minioService;
    
    // 默认分片大小：10MB
    private static final long DEFAULT_CHUNK_SIZE = 10 * 1024 * 1024;
    
    @Override
    @Transactional
    public UploadInitiationVO initiateUpload(MediaUploadInitDTO dto, Long institutionId, Long uploaderId) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 验证文件类型
        MediaType mediaType = determineMediaType(dto.getContentType());
        
        // 验证配额并更新使用的配额
        QuotaType quotaType = mediaTypeToQuotaType(mediaType);
        boolean hasQuota = storageQuotaService.hasEnoughQuota(institutionId, quotaType, dto.getFileSize());
        if (!hasQuota) {
            throw new BusinessException(400, "存储配额不足，无法上传文件");
        } else {
            // 预分配配额
            storageQuotaService.updateUsedQuota(institutionId, quotaType, dto.getFileSize());
        }
        
        try {
            // 生成对象键
            String objectKey = generateObjectKey(institutionId, dto.getFilename(), mediaType);
            
            // 创建Media记录
            Media media = new Media();
            media.setTitle(dto.getTitle());
            media.setDescription(dto.getDescription());
            media.setType(mediaType);
            media.setSize(dto.getFileSize());
            media.setOriginalFilename(dto.getFilename());
            media.setStoragePath(objectKey);
            media.setStatus(MediaStatus.UPLOADING);
            media.setInstitution(institution);
            media.setUploaderId(uploaderId);
            media.setUploadTime(LocalDateTime.now());
            media.setLastAccessTime(LocalDateTime.now());
            
            // 保存Media记录
            Media savedMedia = mediaRepository.save(media);
            
            // 确定分片大小和分片数量
            long chunkSize = dto.getChunkSize() != null ? dto.getChunkSize() : DEFAULT_CHUNK_SIZE;
            int totalParts = calculateTotalParts(dto.getFileSize(), chunkSize);
            
            // 初始化S3分片上传
            String uploadId = s3UploadManager.initiateMultipartUpload(objectKey, dto.getContentType());
            
            // 生成所有分片的预签名URL
            List<PresignedUrlInfo> presignedUrls = s3UploadManager.batchGeneratePresignedUrls(
                    uploadId, objectKey, 1, totalParts);
            
            // 创建并保存上传状态
            UploadStatusInfo statusInfo = UploadStatusInfo.builder()
                    .mediaId(savedMedia.getId())
                    .institutionId(institutionId)
                    .uploaderId(uploaderId)
                    .uploadId(uploadId)
                    .objectKey(objectKey)
                    .filename(dto.getFilename())
                    .contentType(dto.getContentType())
                    .fileSize(dto.getFileSize())
                    .status(MediaStatus.UPLOADING)
                    .totalParts(totalParts)
                    .completedParts(new ArrayList<>())
                    .initiatedAt(LocalDateTime.now())
                    .lastUpdatedAt(LocalDateTime.now())
                    .build();
            
            uploadStatusService.saveUploadStatus(statusInfo);
            
            // 构建返回结果
            return UploadInitiationVO.builder()
                    .mediaId(savedMedia.getId())
                    .uploadId(uploadId)
                    .totalParts(totalParts)
                    .chunkSize(chunkSize)
                    .presignedUrls(presignedUrls)
                    .build();
            
        } catch (Exception e) {
            // 发生异常时释放预分配的配额
            storageQuotaService.updateUsedQuota(institutionId, quotaType, -dto.getFileSize());
            
            log.error("初始化上传失败", e);
            throw new BusinessException(500, "初始化上传失败: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public MediaVO completeUpload(Long mediaId, Long institutionId, CompleteUploadDTO dto) {
        log.info("开始完成上传 - mediaId: {}, institutionId: {}", mediaId, institutionId);
        
        // 验证Media记录
        Media media = getMediaForInstitution(mediaId, institutionId);
        log.info("已验证媒体记录 - mediaId: {}, status: {}", mediaId, media.getStatus());
        
        try {
            log.info("准备完成S3分片上传 - mediaId: {}, uploadId: {}, objectKey: {}, completedParts: {}", 
                    mediaId, dto.getUploadId(), media.getStoragePath(), dto.getCompletedParts().size());
            
            // 打印原始分片信息
            log.info("原始分片信息: {}", dto.getCompletedParts());
            
            // 转换分片信息类型并确保 ETag 格式正确
            List<UploadStatusInfo.PartInfo> s3Parts = dto.getCompletedParts().stream()
                    .map(part -> {
                        // 确保 ETag 有双引号
                        String eTag = part.getETag();
                        log.info("原始ETag值 - partNumber: {}, eTag: '{}'", part.getPartNumber(), eTag);
                        
                        if (eTag != null && !eTag.isEmpty()) {
                            // 移除已有的引号（如果有）
                            eTag = eTag.replace("\"", "");
                            // 确保 ETag 有双引号
                            if (!eTag.startsWith("\"")) {
                                eTag = "\"" + eTag;
                            }
                            if (!eTag.endsWith("\"")) {
                                eTag = eTag + "\"";
                            }
                        }
                        
                        log.info("处理后的ETag值 - partNumber: {}, eTag: '{}'", part.getPartNumber(), eTag);
                        return new UploadStatusInfo.PartInfo(part.getPartNumber(), eTag);
                    })
                    .collect(Collectors.toList());
            
            // 打印处理后的分片信息
            log.info("处理后的分片信息: {}", s3Parts);
            
            // 添加更详细的日志
            s3Parts.forEach(part -> {
                log.info("准备合并的分片 - partNumber: {}, eTag: '{}', eTag长度: {}", 
                        part.getPartNumber(), 
                        part.getETag(),
                        part.getETag() != null ? part.getETag().length() : 0);
            });
            
            // 检查是否所有分片都有有效的ETag
            boolean allPartsHaveETag = s3Parts.stream()
                    .allMatch(part -> part.getETag() != null && !part.getETag().isEmpty());
            log.info("是否所有分片都有有效的ETag: {}", allPartsHaveETag);
            
            // 如果存在ETag为null的分片，抛出更具体的错误
            if (!allPartsHaveETag) {
                log.error("存在ETag为null的分片，无法完成合并");
                throw new BusinessException(400, "无法完成上传：部分分片的ETag为空。请检查前端上传逻辑，确保正确保存每个分片的ETag值。");
            }
            
            // 完成分片上传
            CompleteMultipartUploadResponse response = s3UploadManager.completeMultipartUpload(
                    dto.getUploadId(), media.getStoragePath(), s3Parts);
            
            log.info("S3分片上传完成 - mediaId: {}, response: {}", mediaId, response);
            
            // 清理上传状态（如果存在）
            try {
                uploadStatusService.deleteUploadStatus(mediaId);
            } catch (Exception e) {
                log.warn("清理上传状态失败，可能不存在 - mediaId: {}", mediaId);
                // 忽略此错误，继续处理
            }
            
            // 更新Media状态
            media.setStatus(MediaStatus.COMPLETED);
            media.setLastAccessTime(LocalDateTime.now());
            Media updatedMedia = mediaRepository.save(media);
            
            log.info("媒体状态已更新为已完成 - mediaId: {}", mediaId);
            
            // 返回媒体信息
            return mapToMediaVO(updatedMedia, null);
            
        } catch (Exception e) {
            log.error("完成上传失败 - mediaId: {}, error: {}", mediaId, e.getMessage(), e);
            
            // 更新Media状态为失败
            media.setStatus(MediaStatus.FAILED);
            mediaRepository.save(media);
            
            throw new BusinessException(500, "完成上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void cancelUpload(Long mediaId, Long institutionId) {
        // 验证Media记录
        Media media = getMediaForInstitution(mediaId, institutionId);
        
        try {
            // 获取上传状态
            UploadStatusInfo statusInfo = uploadStatusService.getUploadStatusOrNull(mediaId);
            
            if (statusInfo != null) {
                // 取消S3分片上传
                s3UploadManager.abortMultipartUpload(
                        statusInfo.getUploadId(), statusInfo.getObjectKey());
                
                // 删除上传状态
                uploadStatusService.deleteUploadStatus(mediaId);
            }
            
            // 计算要释放的配额
            MediaType mediaType = media.getType();
            QuotaType quotaType = mediaTypeToQuotaType(mediaType);
            Long fileSize = media.getSize();
            
            // 释放预分配的配额
            storageQuotaService.updateUsedQuota(institutionId, quotaType, -fileSize);
            
            // 删除Media记录
            mediaRepository.delete(media);
            
        } catch (Exception e) {
            log.error("取消上传失败", e);
            throw new BusinessException(500, "取消上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getMediaAccessUrl(Long mediaId, Long institutionId, Long expirationMinutes) {
        // 验证Media记录
        Media media = getMediaForInstitution(mediaId, institutionId);
        
        // 检查媒体状态
        if (media.getStatus() != MediaStatus.COMPLETED) {
            throw new BusinessException(400, "媒体文件未上传完成，无法获取访问URL");
        }
        
        // 生成预签名URL
        String url = s3UploadManager.generatePresignedGetUrl(
                media.getStoragePath(), expirationMinutes);
        
        // 更新最后访问时间
        media.setLastAccessTime(LocalDateTime.now());
        mediaRepository.save(media);
        
        return url;
    }
    
    @Override
    @Transactional(readOnly = true)
    public MediaVO getMediaInfo(Long mediaId, Long institutionId) {
        log.info("开始获取媒体信息, mediaId: {}, institutionId: {}", mediaId, institutionId);
        
        // 验证Media记录
        Media media = getMediaForInstitution(mediaId, institutionId);
        
        // 转换为VO
        MediaVO mediaVO = mapToMediaVO(media, null);
        log.info("成功获取媒体信息: {}", mediaVO);
        return mediaVO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MediaVO> getMediaList(Long institutionId, Pageable pageable) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 查询机构的媒体列表
        Page<Media> mediaPage = mediaRepository.findByInstitution(institution, pageable);
        
        // 转换为VO
        return mediaPage.map(media -> mapToMediaVO(media, null));
    }
    
    @Override
    @Transactional
    public void deleteMedia(Long mediaId, Long institutionId) {
        // 获取媒体信息，验证权限
        Media media = getMediaForInstitution(mediaId, institutionId);
        
        // 从 Minio 存储中删除文件
        try {
            boolean deleted = minioService.deleteFile(media.getStoragePath());
            if (!deleted) {
                log.warn("MinIO中未找到要删除的文件, objectKey: {}", media.getStoragePath());
            }
            
            // 归还存储配额
            QuotaType quotaType = mediaTypeToQuotaType(media.getType());
            storageQuotaService.updateUsedQuota(institutionId, quotaType, -media.getSize());
            
            // 从数据库中删除记录
            mediaRepository.delete(media);
            
            log.info("成功删除媒体文件, mediaId: {}, institutionId: {}", mediaId, institutionId);
        } catch (Exception e) {
            log.error("删除媒体文件失败, mediaId: {}, institutionId: {}, error: {}", mediaId, institutionId, e.getMessage());
            throw new BusinessException("删除媒体文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取机构的Media记录
     * 
     * @param mediaId 媒体ID
     * @param institutionId 机构ID
     * @return Media记录
     */
    private Media getMediaForInstitution(Long mediaId, Long institutionId) {
        log.info("开始验证媒体所属机构, mediaId: {}, institutionId: {}", mediaId, institutionId);
        
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> {
                    log.error("机构不存在, institutionId: {}", institutionId);
                    return new ResourceNotFoundException("机构不存在，ID: " + institutionId);
                });
        
        log.info("成功找到机构: {}", institution.getName());
        
        return mediaRepository.findByIdAndInstitution(mediaId, institution)
                .orElseThrow(() -> {
                    log.error("媒体文件不存在或不属于该机构, mediaId: {}, institutionId: {}", mediaId, institutionId);
                    return new ResourceNotFoundException("媒体文件不存在或不属于该机构，ID: " + mediaId);
                });
    }
    
    /**
     * 根据内容类型确定媒体类型
     * 
     * @param contentType 内容类型
     * @return 媒体类型
     */
    private MediaType determineMediaType(String contentType) {
        if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        } else if (contentType.startsWith("audio/")) {
            return MediaType.AUDIO;
        } else {
            return MediaType.DOCUMENT;
        }
    }
    
    /**
     * 媒体类型转换为配额类型
     * 
     * @param mediaType 媒体类型
     * @return 配额类型
     */
    private QuotaType mediaTypeToQuotaType(MediaType mediaType) {
        if (mediaType == MediaType.VIDEO) {
            return QuotaType.VIDEO;
        } else {
            return QuotaType.DOCUMENT;
        }
    }
    
    /**
     * 生成对象键
     * 
     * @param institutionId 机构ID
     * @param originalFilename 原始文件名
     * @param mediaType 媒体类型
     * @return 对象键
     */
    private String generateObjectKey(Long institutionId, String originalFilename, MediaType mediaType) {
        String type = mediaType.name().toLowerCase();
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%s/%s/%s", type, institutionId, uuid, originalFilename);
    }
    
    /**
     * 计算总分片数
     * 
     * @param fileSize 文件大小
     * @param chunkSize 分片大小
     * @return 总分片数
     */
    private int calculateTotalParts(Long fileSize, Long chunkSize) {
        int totalParts = (int) (fileSize / chunkSize);
        if (fileSize % chunkSize != 0) {
            totalParts++;
        }
        return totalParts;
    }
    
    /**
     * 构建MediaVO
     * 
     * @param media 媒体实体
     * @param accessUrl 访问URL（可选）
     * @return 媒体VO
     */
    private MediaVO mapToMediaVO(Media media, String accessUrl) {
        return MediaVO.builder()
                .id(media.getId())
                .title(media.getTitle())
                .description(media.getDescription())
                .type(media.getType() != null ? media.getType().name() : null)
                .size(media.getSize())
                .originalFilename(media.getOriginalFilename())
                .status(media.getStatus() != null ? media.getStatus().name() : null)
                .institutionId(media.getInstitution() != null ? media.getInstitution().getId() : null)
                .uploaderId(media.getUploaderId())
                .uploadTime(media.getUploadTime())
                .lastAccessTime(media.getLastAccessTime())
                .accessUrl(accessUrl)
                .build();
    }
} 