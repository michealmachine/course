package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.config.CacheConfig;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.media.*;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Media;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.AdminMediaVO;
import com.zhangziqi.online_course_mine.model.vo.MediaActivityCalendarVO;
import com.zhangziqi.online_course_mine.model.vo.MediaTypeDistributionVO;
import com.zhangziqi.online_course_mine.model.vo.MediaVO;
import com.zhangziqi.online_course_mine.model.vo.StorageGrowthPointVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.MediaRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.MediaService;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import com.zhangziqi.online_course_mine.service.MinioService;
import com.zhangziqi.online_course_mine.utils.FormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zhangziqi.online_course_mine.model.enums.ContentType.IMAGE;

/**
 * 媒体服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
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

        // 验证文件类型，传入文件名以便更准确地判断
        MediaType mediaType = determineMediaType(dto.getContentType(), dto.getFilename());

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

    @Transactional(readOnly = true)
    @Override
    public Page<MediaVO> getMediaList(Long institutionId, MediaType type, String filename, Pageable pageable) {
        log.info("根据条件获取机构媒体列表 - 机构ID: {}, 媒体类型: {}, 文件名关键词: {}",
                institutionId, type, filename);

        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));

        // 根据提供的参数选择对应的查询方法
        Page<Media> mediaPage;

        if (type != null && filename != null && !filename.isEmpty()) {
            // 同时按类型和文件名筛选
            mediaPage = mediaRepository.findByInstitutionAndTypeAndOriginalFilenameContaining(
                    institution, type, filename, pageable);
            log.info("按机构、类型和文件名查询 - 结果数: {}", mediaPage.getTotalElements());
        } else if (type != null) {
            // 仅按类型筛选
            mediaPage = mediaRepository.findByInstitutionAndType(institution, type, pageable);
            log.info("按机构和类型查询 - 结果数: {}", mediaPage.getTotalElements());
        } else if (filename != null && !filename.isEmpty()) {
            // 仅按文件名筛选
            mediaPage = mediaRepository.findByInstitutionAndOriginalFilenameContaining(
                    institution, filename, pageable);
            log.info("按机构和文件名查询 - 结果数: {}", mediaPage.getTotalElements());
        } else {
            // 不筛选，获取所有
            mediaPage = mediaRepository.findByInstitution(institution, pageable);
            log.info("仅按机构查询 - 结果数: {}", mediaPage.getTotalElements());
        }

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
     * 根据内容类型和文件名确定媒体类型
     *
     * @param contentType 内容类型
     * @param filename 文件名（可选）
     * @return 媒体类型
     */
    private MediaType determineMediaType(String contentType, String filename) {
        // 1. 首先通过MIME类型判断
        if (contentType != null) {
            if (contentType.startsWith("video/")) {
                return MediaType.VIDEO;
            } else if (contentType.startsWith("audio/")) {
                return MediaType.AUDIO;
            } else if (contentType.startsWith("image/")) {
                return MediaType.IMAGE;
            } else if (contentType.startsWith("application/pdf") ||
                      contentType.contains("word") ||
                      contentType.contains("excel") ||
                      contentType.contains("powerpoint") ||
                      contentType.contains("text/")) {
                return MediaType.DOCUMENT;
            }
        }

        // 2. 如果无法通过MIME类型判断，尝试从文件名中提取扩展名
        if (filename != null && filename.contains(".")) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

            // 视频格式
            if (extension.matches("mp4|avi|mov|wmv|flv|mkv|webm")) {
                return MediaType.VIDEO;
            }
            // 音频格式
            else if (extension.matches("mp3|wav|ogg|aac|flac|m4a")) {
                return MediaType.AUDIO;
            }
            // 图片格式
            else if (extension.matches("jpg|jpeg|png|gif|bmp|svg|webp")) {
                return MediaType.IMAGE;
            }
            // 文档格式
            else if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|csv|rtf")) {
                return MediaType.DOCUMENT;
            }
        }

        // 默认为文档类型
        return MediaType.DOCUMENT;
    }

    /**
     * 根据内容类型确定媒体类型（向后兼容）
     *
     * @param contentType 内容类型
     * @return 媒体类型
     */
    private MediaType determineMediaType(String contentType) {
        return determineMediaType(contentType, null);
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
        } else if (mediaType == MediaType.IMAGE || mediaType == MediaType.AUDIO) {
            // 图片和音频也使用文档配额
            return QuotaType.DOCUMENT;
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

    @Override
    @Transactional(readOnly = true)
    public MediaVO getMediaByIdForPreview(Long mediaId) {
        log.info("开始获取媒体信息(预览模式), mediaId: {}", mediaId);

        // 直接通过ID查找媒体，不验证机构
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> {
                    log.error("媒体资源不存在, mediaId: {}", mediaId);
                    return new ResourceNotFoundException("媒体资源不存在，ID: " + mediaId);
                });

        // 检查媒体状态
        if (media.getStatus() != MediaStatus.COMPLETED) {
            log.warn("媒体文件未上传完成，无法访问, mediaId: {}, status: {}", mediaId, media.getStatus());
            throw new BusinessException(400, "媒体文件未上传完成，无法访问");
        }

        // 生成临时访问URL (默认30分钟)
        String url = s3UploadManager.generatePresignedGetUrl(
                media.getStoragePath(), 30L);

        // 更新最后访问时间
        media.setLastAccessTime(LocalDateTime.now());
        mediaRepository.save(media);

        log.info("成功获取媒体信息(预览模式): {}, URL已生成", media.getTitle());

        // 返回包含URL的VO
        return mapToMediaVO(media, url);
    }

    /**
     * 获取指定机构的媒体活动日历数据
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.MEDIA_ACTIVITY_CACHE, key = "'institution_' + #institutionId + '_' + #startDate + '_' + #endDate")
    public MediaActivityCalendarVO getMediaActivityCalendar(Long institutionId, LocalDate startDate, LocalDate endDate) {
        log.info("获取机构媒体活动日历数据 - 机构ID: {}, 开始日期: {}, 结束日期: {}", institutionId, startDate, endDate);

        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));

        // 转换日期范围为 LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询日期范围内的媒体上传活动
        List<MediaActivityDTO> activities = mediaRepository.findMediaUploadActivitiesByInstitution(
                institutionId, startDateTime, endDateTime);

        log.info("查询到 {} 条媒体活动记录", activities.size());

        // 构建日历数据
        return buildCalendarData(activities, startDate, endDate);
    }

    /**
     * 获取所有机构的媒体活动日历数据
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.MEDIA_ACTIVITY_CACHE, key = "'all_institutions_' + #startDate + '_' + #endDate")
    public MediaActivityCalendarVO getAllMediaActivityCalendar(LocalDate startDate, LocalDate endDate) {
        log.info("获取所有机构媒体活动日历数据 - 开始日期: {}, 结束日期: {}", startDate, endDate);

        // 转换日期范围为 LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询日期范围内的媒体上传活动
        List<MediaActivityDTO> activities = mediaRepository.findAllMediaUploadActivities(
                startDateTime, endDateTime);

        log.info("查询到 {} 条媒体活动记录", activities.size());

        // 构建日历数据
        return buildCalendarData(activities, startDate, endDate);
    }

    /**
     * 构建日历数据
     */
    private MediaActivityCalendarVO buildCalendarData(List<MediaActivityDTO> activities, LocalDate startDate, LocalDate endDate) {
        // 如果没有活动数据，返回空的日历数据
        if (activities == null || activities.isEmpty()) {
            log.info("没有媒体活动数据，返回空日历");
            return MediaActivityCalendarVO.builder()
                    .calendarData(new ArrayList<>())
                    .peakCount(0L)
                    .totalCount(0L)
                    .totalSize(0L)
                    .build();
        }

        // 找出峰值活动和最活跃日期
        MediaActivityDTO peakActivity = activities.stream()
                .max(Comparator.comparing(MediaActivityDTO::getCount))
                .orElse(null);

        // 计算总活动数和总文件大小
        long totalCount = activities.stream().mapToLong(MediaActivityDTO::getCount).sum();
        long totalSize = activities.stream().mapToLong(MediaActivityDTO::getTotalSize).sum();

        log.info("峰值活动: {} 个文件，日期: {}",
                peakActivity != null ? peakActivity.getCount() : 0,
                peakActivity != null ? peakActivity.getDate() : "无");
        log.info("总活动数: {} 个文件，总大小: {} 字节", totalCount, totalSize);

        // 构建返回结果
        return MediaActivityCalendarVO.builder()
                .calendarData(activities)
                .peakCount(peakActivity != null ? peakActivity.getCount() : 0L)
                .mostActiveDate(peakActivity != null ? peakActivity.getDate() : null)
                .totalCount(totalCount)
                .totalSize(totalSize)
                .build();
    }

    /**
     * 根据日期获取指定机构的媒体列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MediaVO> getMediaListByDate(Long institutionId, LocalDate date, Pageable pageable) {
        log.info("根据日期获取机构媒体列表 - 机构ID: {}, 日期: {}", institutionId, date);

        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));

        // 查询指定日期上传的媒体
        Page<Media> mediaPage = mediaRepository.findMediaByInstitutionAndDate(institutionId, date, pageable);

        log.info("查询到 {} 条媒体记录", mediaPage.getTotalElements());

        // 转换为 VO
        return mediaPage.map(media -> mapToMediaVO(media, null));
    }

    /**
     * 根据日期获取所有机构的媒体列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MediaVO> getAllMediaListByDate(LocalDate date, Pageable pageable) {
        log.info("根据日期获取所有机构媒体列表 - 日期: {}", date);

        // 查询指定日期上传的所有媒体
        Page<Media> mediaPage = mediaRepository.findAllMediaByDate(date, pageable);

        log.info("查询到 {} 条媒体记录", mediaPage.getTotalElements());

        // 转换为 VO
        return mediaPage.map(media -> mapToMediaVO(media, null));
    }

    /**
     * 获取所有机构的媒体列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MediaVO> getAllMediaList(MediaType type, String filename, Pageable pageable) {
        log.info("管理员查询所有机构媒体列表 - 类型: {}, 文件名: {}, 分页: {}", type, filename, pageable);

        // 构建动态查询条件
        Specification<Media> spec = Specification.where(null); // Start with empty spec

        if (type != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("type"), type)
            );
        }

        if (StringUtils.hasText(filename)) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("originalFilename")), "%" + filename.toLowerCase() + "%")
            );
        }

        // 执行查询
        Page<Media> mediaPage = mediaRepository.findAll(spec, pageable);

        log.info("查询到符合条件的媒体记录 {} 条", mediaPage.getTotalElements());

        // 映射为 VO
        return mediaPage.map(media -> mapToMediaVO(media, null)); // Assuming mapToMediaVO exists
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageGrowthPointVO> getStorageGrowthTrend(
            LocalDate startDate, LocalDate endDate, ChronoUnit granularity) {

        log.info("获取存储增长趋势数据, 开始日期: {}, 结束日期: {}, 粒度: {}",
                 startDate, endDate, granularity);

        // 将 LocalDate 转换为 LocalDateTime 以匹配 Repository 方法
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // 结束日期需要包含当天

        // TODO: 实现不同粒度的支持 (WEEKLY, MONTHLY) - 目前仅支持 DAILY
        if (granularity != ChronoUnit.DAYS) {
            log.warn("目前 getStorageGrowthTrend 仅支持 DAILY 粒度");
            // 或者抛出异常 throw new UnsupportedOperationException("Granularity not supported yet");
        }

        // 调用 Repository 获取每日上传活动数据
        List<MediaActivityDTO> dailyActivities = mediaRepository.findAllMediaUploadActivities(
                startDateTime, endDateTime);

        // 将 MediaActivityDTO 映射为 StorageGrowthPointVO
        List<StorageGrowthPointVO> trendData = dailyActivities.stream()
                .map(dto -> StorageGrowthPointVO.builder()
                        .date(dto.getDate())
                        .sizeAdded(dto.getTotalSize()) // 使用当天上传的总大小
                        .build())
                .collect(Collectors.toList());

        log.info("成功获取存储增长趋势数据点 {} 个", trendData.size());
        return trendData;
    }

    /**
     * 将Media转换为AdminMediaVO（包含额外的机构名称和上传者名称信息）
     */
    private AdminMediaVO mapToAdminMediaVO(Media media, String accessUrl) {
        // 先获取基础的MediaVO信息
        MediaVO baseVO = mapToMediaVO(media, accessUrl);

        // 获取机构名称
        String institutionName = "";
        if (media.getInstitution() != null) {
            institutionName = media.getInstitution().getName();
        }

        // 尝试获取上传者名称
        String uploaderUsername = "";
        if (media.getUploaderId() != null) {
            // 从用户仓库查询用户名
            try {
                User uploader = userRepository.findById(media.getUploaderId()).orElse(null);
                if (uploader != null) {
                    uploaderUsername = uploader.getUsername();
                }
            } catch (Exception e) {
                log.warn("获取上传者名称时出错: {}", e.getMessage());
                // 不抛出异常，使用空名称
            }
        }

        // 格式化文件大小
        String formattedSize = FormatUtil.formatFileSize(media.getSize());

        // 构建扩展VO
        return AdminMediaVO.builder()
                // 继承基础VO的所有属性
                .id(baseVO.getId())
                .title(baseVO.getTitle())
                .description(baseVO.getDescription())
                .type(baseVO.getType())
                .size(baseVO.getSize())
                .originalFilename(baseVO.getOriginalFilename())
                .status(baseVO.getStatus())
                .institutionId(baseVO.getInstitutionId())
                .uploaderId(baseVO.getUploaderId())
                .uploadTime(baseVO.getUploadTime())
                .lastAccessTime(baseVO.getLastAccessTime())
                .accessUrl(baseVO.getAccessUrl())
                // 添加扩展属性
                .institutionName(institutionName)
                .uploaderUsername(uploaderUsername)
                .formattedSize(formattedSize)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminMediaVO> getAdminMediaList(
            MediaType type,
            String filename,
            String institutionName,
            LocalDateTime uploadStartTime,
            LocalDateTime uploadEndTime,
            Long minSize,
            Long maxSize,
            Pageable pageable) {

        log.info("管理员高级查询媒体列表 - 类型: {}, 文件名: {}, 机构名称: {}, 上传时间: {} 至 {}, 大小: {} 至 {}",
                type, filename, institutionName, uploadStartTime, uploadEndTime, minSize, maxSize);

        // 构建动态查询条件
        Specification<Media> spec = Specification.where(null);

        // 按媒体类型筛选
        if (type != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("type"), type)
            );
        }

        // 按文件名筛选
        if (StringUtils.hasText(filename)) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("originalFilename")), "%" + filename.toLowerCase() + "%")
            );
        }

        // 按机构名称筛选
        if (StringUtils.hasText(institutionName)) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("institution").get("name")), "%" + institutionName.toLowerCase() + "%")
            );
        }

        // 按上传时间筛选
        if (uploadStartTime != null && uploadEndTime != null) {
            spec = spec.and((root, query, cb) ->
                cb.between(root.get("uploadTime"), uploadStartTime, uploadEndTime)
            );
        } else if (uploadStartTime != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("uploadTime"), uploadStartTime)
            );
        } else if (uploadEndTime != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("uploadTime"), uploadEndTime)
            );
        }

        // 按文件大小筛选
        if (minSize != null && maxSize != null) {
            spec = spec.and((root, query, cb) ->
                cb.between(root.get("size"), minSize, maxSize)
            );
        } else if (minSize != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("size"), minSize)
            );
        } else if (maxSize != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("size"), maxSize)
            );
        }

        // 执行查询
        Page<Media> mediaPage = mediaRepository.findAll(spec, pageable);

        log.info("查询到符合条件的媒体记录 {} 条", mediaPage.getTotalElements());

        // 映射为扩展VO
        return mediaPage.map(media -> mapToAdminMediaVO(media, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminMediaVO> getAdminMediaListByDate(LocalDate date, Pageable pageable) {
        log.info("管理员根据日期获取媒体列表 - 日期: {}", date);

        // 查询指定日期上传的所有媒体
        Page<Media> mediaPage = mediaRepository.findAllMediaByDate(date, pageable);

        log.info("查询到 {} 条媒体记录", mediaPage.getTotalElements());

        // 转换为扩展VO
        return mediaPage.map(media -> mapToAdminMediaVO(media, null));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.MEDIA_STATS_CACHE, key = "'type_distribution_' + (T(java.util.Objects).isNull(#institutionId) ? 'all' : #institutionId)")
    public MediaTypeDistributionVO getMediaTypeDistribution(Long institutionId) {
        log.info("获取媒体类型分布统计 - 机构ID: {}", institutionId);

        List<Object[]> typeCountList;

        // 根据是否指定机构ID选择不同的查询方法
        if (institutionId != null) {
            Institution institution = institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
            typeCountList = mediaRepository.countByMediaTypeForInstitution(institutionId);
        } else {
            typeCountList = mediaRepository.countByMediaType();
        }

        // 解析查询结果
        Map<MediaType, Long> typeCountMap = new HashMap<>();
        long totalCount = 0;

        for (Object[] result : typeCountList) {
            MediaType type = (MediaType) result[0];
            Long count = ((Number) result[1]).longValue();
            typeCountMap.put(type, count);
            totalCount += count;
        }

        // 构建分布详情列表
        List<MediaTypeDistributionVO.TypeDistribution> distribution = new ArrayList<>();

        // 确保所有媒体类型都有记录
        for (MediaType type : MediaType.values()) {
            long count = typeCountMap.getOrDefault(type, 0L);
            double percentage = totalCount > 0 ? (double) count / totalCount : 0;

            distribution.add(
                MediaTypeDistributionVO.TypeDistribution.builder()
                    .type(type)
                    .typeName(getMediaTypeName(type))
                    .count(count)
                    .percentage(percentage)
                    .build()
            );
        }

        // 按数量降序排序
        distribution.sort(Comparator.comparing(MediaTypeDistributionVO.TypeDistribution::getCount).reversed());

        return MediaTypeDistributionVO.builder()
                .totalCount(totalCount)
                .typeCount(typeCountMap)
                .distribution(distribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.MEDIA_STATS_CACHE, key = "'institution_storage_usage'")
    public Map<String, Long> getInstitutionStorageUsage() {
        log.info("获取各机构的媒体存储占用统计");

        // 获取所有机构
        List<Institution> institutions = institutionRepository.findAll();
        Map<String, Long> usageMap = new HashMap<>();

        // 查询每个机构的存储使用量
        for (Institution institution : institutions) {
            Long usage = mediaRepository.sumSizeByInstitution(institution);
            // 为null时设为0
            usageMap.put(institution.getName(), usage != null ? usage : 0L);
        }

        log.info("成功获取 {} 个机构的存储使用情况", usageMap.size());
        return usageMap;
    }

    /**
     * 获取媒体类型的中文名称
     */
    private String getMediaTypeName(MediaType type) {
        switch (type) {
            case VIDEO:
                return "视频";
            case AUDIO:
                return "音频";
            case IMAGE:
                return "图片";
            case DOCUMENT:
                return "文档";
            default:
                return "未知";
        }
    }
}