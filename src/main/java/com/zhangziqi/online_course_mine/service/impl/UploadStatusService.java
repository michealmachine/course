package com.zhangziqi.online_course_mine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.media.UploadStatusInfo;
import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.List;

/**
 * 上传状态服务
 * 用于管理媒体上传状态信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadStatusService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 上传状态在Redis中的过期时间（小时）
    private static final int STATUS_EXPIRATION_HOURS = 24;
    
    // Redis键前缀
    private static final String UPLOAD_STATUS_KEY_PREFIX = "upload_status:";
    
    /**
     * 保存上传状态
     * 
     * @param statusInfo 上传状态信息
     */
    public void saveUploadStatus(UploadStatusInfo statusInfo) {
        String key = generateKey(statusInfo.getMediaId());
        
        // 计算过期时间
        LocalDateTime expiresAt = LocalDateTime.now().plus(STATUS_EXPIRATION_HOURS, ChronoUnit.HOURS);
        statusInfo.setExpiresAt(expiresAt);
        
        // 保存状态
        redisTemplate.opsForValue().set(key, statusInfo);
        redisTemplate.expire(key, STATUS_EXPIRATION_HOURS, TimeUnit.HOURS);
        
        log.info("Saved upload status for media ID: {}, upload ID: {}", statusInfo.getMediaId(), statusInfo.getUploadId());
    }
    
    /**
     * 获取上传状态
     * 
     * @param mediaId 媒体ID
     * @return 上传状态信息
     */
    public UploadStatusInfo getUploadStatus(Long mediaId) {
        String key = generateKey(mediaId);
        
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            throw new ResourceNotFoundException("上传状态不存在，媒体ID: " + mediaId);
        }
        
        UploadStatusInfo statusInfo;
        if (value instanceof UploadStatusInfo) {
            statusInfo = (UploadStatusInfo) value;
        } else {
            // 处理可能的序列化/反序列化问题
            try {
                String json = objectMapper.writeValueAsString(value);
                statusInfo = objectMapper.readValue(json, UploadStatusInfo.class);
            } catch (Exception e) {
                log.error("Failed to convert Redis value to UploadStatusInfo", e);
                throw new ResourceNotFoundException("上传状态格式错误，媒体ID: " + mediaId);
            }
        }
        
        return statusInfo;
    }
    
    /**
     * 获取上传状态，如果不存在则返回null
     * 
     * @param mediaId 媒体ID
     * @return 上传状态信息或null
     */
    public UploadStatusInfo getUploadStatusOrNull(Long mediaId) {
        try {
            return getUploadStatus(mediaId);
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }
    
    /**
     * 删除上传状态
     * 
     * @param mediaId 媒体ID
     */
    public void deleteUploadStatus(Long mediaId) {
        String key = generateKey(mediaId);
        redisTemplate.delete(key);
        log.info("Deleted upload status for media ID: {}", mediaId);
    }
    
    /**
     * 更新分片信息
     * 
     * @param mediaId 媒体ID
     * @param partInfo 分片信息
     * @return 更新后的上传状态信息
     */
    public UploadStatusInfo updatePartInfo(Long mediaId, UploadStatusInfo.PartInfo partInfo) {
        log.info("开始更新分片信息 - mediaId: {}, partNumber: {}", mediaId, partInfo.getPartNumber());
        
        String key = generateKey(mediaId);
        
        return redisTemplate.execute(new SessionCallback<UploadStatusInfo>() {
            @Override
            @SuppressWarnings("unchecked")
            public UploadStatusInfo execute(RedisOperations operations) {
                try {
                    // 开启事务监控
                    operations.watch(key);
                    
                    // 获取当前状态
                    UploadStatusInfo statusInfo = getUploadStatus(mediaId);
                    log.info("当前上传状态 - mediaId: {}, 总分片数: {}, 已完成分片数: {}, 已完成分片列表: {}", 
                            mediaId, 
                            statusInfo.getTotalParts(),
                            statusInfo.getCompletedParts().size(),
                            statusInfo.getCompletedParts().stream()
                                    .map(UploadStatusInfo.PartInfo::getPartNumber)
                                    .collect(Collectors.toList()));
                    
                    // 检查分片是否已存在
                    boolean exists = statusInfo.getCompletedParts().stream()
                            .anyMatch(part -> part.getPartNumber() == partInfo.getPartNumber());
                    
                    if (!exists) {
                        // 开启事务
                        operations.multi();
                        
                        // 添加新完成的分片
                        statusInfo.getCompletedParts().add(partInfo);
                        statusInfo.setLastUpdatedAt(LocalDateTime.now());
                        
                        // 计算过期时间
                        LocalDateTime expiresAt = LocalDateTime.now().plus(STATUS_EXPIRATION_HOURS, ChronoUnit.HOURS);
                        statusInfo.setExpiresAt(expiresAt);
                        
                        // 保存更新后的状态
                        operations.opsForValue().set(key, statusInfo);
                        operations.expire(key, STATUS_EXPIRATION_HOURS, TimeUnit.HOURS);
                        
                        // 执行事务
                        List<Object> results = operations.exec();
                        
                        if (results != null) {
                            log.info("分片信息更新完成 - mediaId: {}, partNumber: {}, 当前已完成分片数: {}, 已完成分片列表: {}", 
                                    mediaId, 
                                    partInfo.getPartNumber(), 
                                    statusInfo.getCompletedParts().size(),
                                    statusInfo.getCompletedParts().stream()
                                            .map(UploadStatusInfo.PartInfo::getPartNumber)
                                            .collect(Collectors.toList()));
                        } else {
                            log.warn("事务执行失败，可能是并发更新导致 - mediaId: {}, partNumber: {}", mediaId, partInfo.getPartNumber());
                            // 重新获取最新状态
                            return getUploadStatus(mediaId);
                        }
                    } else {
                        log.info("分片已存在，跳过更新 - mediaId: {}, partNumber: {}", mediaId, partInfo.getPartNumber());
                        operations.unwatch();
                    }
                    
                    return statusInfo;
                    
                } catch (Exception e) {
                    log.error("更新分片信息失败 - mediaId: {}, partNumber: {}, error: {}", 
                            mediaId, partInfo.getPartNumber(), e.getMessage());
                    throw new RuntimeException("更新分片信息失败", e);
                }
            }
        });
    }
    
    /**
     * 更新上传状态
     * 
     * @param mediaId 媒体ID
     * @param status 新状态
     * @return 更新后的上传状态信息
     */
    public UploadStatusInfo updateStatus(Long mediaId, MediaStatus status) {
        UploadStatusInfo statusInfo = getUploadStatus(mediaId);
        statusInfo.setStatus(status);
        statusInfo.setLastUpdatedAt(LocalDateTime.now());
        
        // 保存更新后的状态
        saveUploadStatus(statusInfo);
        
        log.debug("已更新上传状态: mediaId={}, newStatus={}", mediaId, status);
        
        return statusInfo;
    }
    
    /**
     * 生成Redis键
     * 
     * @param mediaId 媒体ID
     * @return Redis键
     */
    private String generateKey(Long mediaId) {
        return UPLOAD_STATUS_KEY_PREFIX + mediaId;
    }
} 