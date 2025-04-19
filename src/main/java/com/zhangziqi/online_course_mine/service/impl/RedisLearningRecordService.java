package com.zhangziqi.online_course_mine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis学习记录服务
 * 负责在Redis中临时存储学习记录，以减少数据库写入次数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLearningRecordService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis键前缀
    private static final String LEARNING_RECORD_KEY_PREFIX = "learning:record:";
    
    // 记录过期时间（天）
    private static final int RECORD_EXPIRATION_DAYS = 3;
    
    /**
     * 更新学习记录
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param sectionId 小节ID
     * @param activityType 活动类型
     * @param durationSeconds 持续时间（秒）
     * @param contextData 上下文数据
     */
    public void updateLearningRecord(Long userId, Long courseId, Long chapterId, Long sectionId, 
                                    String activityType, int durationSeconds, String contextData) {
        // 生成当前日期字符串
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        // 构建Redis键
        String redisKey = buildRedisKey(dateStr, userId, courseId, activityType);
        
        // 使用Hash结构存储数据
        redisTemplate.opsForHash().increment(redisKey, "totalDuration", durationSeconds);
        redisTemplate.opsForHash().put(redisKey, "lastUpdate", System.currentTimeMillis());
        
        // 更新章节和小节信息
        if (chapterId != null) {
            redisTemplate.opsForHash().put(redisKey, "chapterId", chapterId.toString());
        }
        
        if (sectionId != null) {
            redisTemplate.opsForHash().put(redisKey, "sectionId", sectionId.toString());
        }
        
        // 更新上下文数据（可选）
        if (contextData != null) {
            redisTemplate.opsForHash().put(redisKey, "contextData", contextData);
        }
        
        // 设置过期时间
        redisTemplate.expire(redisKey, RECORD_EXPIRATION_DAYS, TimeUnit.DAYS);
        
        log.debug("更新Redis学习记录: {}, 用户: {}, 课程: {}, 活动: {}, 时长: {}秒", 
                redisKey, userId, courseId, activityType, durationSeconds);
    }
    
    /**
     * 构建Redis键
     */
    private String buildRedisKey(String dateStr, Long userId, Long courseId, String activityType) {
        return LEARNING_RECORD_KEY_PREFIX + dateStr + ":" + userId + ":" + courseId + ":" + activityType;
    }
    
    /**
     * 获取指定日期的所有学习记录键
     */
    public Set<String> getLearningRecordKeys(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        String pattern = LEARNING_RECORD_KEY_PREFIX + dateStr + ":*";
        return redisTemplate.keys(pattern);
    }
    
    /**
     * 获取学习记录数据
     */
    public Map<Object, Object> getLearningRecordData(String redisKey) {
        return redisTemplate.opsForHash().entries(redisKey);
    }
    
    /**
     * 删除学习记录
     */
    public void deleteLearningRecord(String redisKey) {
        redisTemplate.delete(redisKey);
    }
}
