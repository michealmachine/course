package com.zhangziqi.online_course_mine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.media.UploadStatusInfo;
import com.zhangziqi.online_course_mine.model.enums.MediaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * 上传状态服务
 * 用于管理媒体上传状态信息
 * 注：仅用于内部跟踪上传状态，不提供前端状态管理
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
    private UploadStatusInfo getUploadStatus(Long mediaId) {
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
     * 生成Redis键
     * 
     * @param mediaId 媒体ID
     * @return Redis键
     */
    private String generateKey(Long mediaId) {
        return UPLOAD_STATUS_KEY_PREFIX + mediaId;
    }
} 