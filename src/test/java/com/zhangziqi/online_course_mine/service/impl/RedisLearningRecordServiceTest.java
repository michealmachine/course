package com.zhangziqi.online_course_mine.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisLearningRecordServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private RedisLearningRecordService redisLearningRecordService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("更新学习记录")
    void testUpdateLearningRecord() {
        // 准备测试数据
        Long userId = 1L;
        Long courseId = 2L;
        Long chapterId = 3L;
        Long sectionId = 4L;
        String activityType = "VIDEO_WATCH";
        int durationSeconds = 120;
        String contextData = "{\"progress\":50}";

        // 执行方法
        redisLearningRecordService.updateLearningRecord(
                userId, courseId, chapterId, sectionId, activityType, durationSeconds, contextData);

        // 验证Redis操作
        String expectedKey = "learning:record:" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) +
                ":" + userId + ":" + courseId + ":" + activityType;

        verify(hashOperations).increment(eq(expectedKey), eq("totalDuration"), eq((long) durationSeconds));
        verify(hashOperations).put(eq(expectedKey), eq("lastUpdate"), any(Long.class));
        verify(hashOperations).put(eq(expectedKey), eq("chapterId"), eq(chapterId.toString()));
        verify(hashOperations).put(eq(expectedKey), eq("sectionId"), eq(sectionId.toString()));
        verify(hashOperations).put(eq(expectedKey), eq("contextData"), eq(contextData));
        verify(redisTemplate).expire(eq(expectedKey), eq(3L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("获取学习记录键")
    void testGetLearningRecordKeys() {
        // 准备测试数据
        LocalDate date = LocalDate.of(2023, 6, 1);
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        String pattern = "learning:record:" + dateStr + ":*";

        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add("learning:record:" + dateStr + ":1:2:VIDEO_WATCH");
        expectedKeys.add("learning:record:" + dateStr + ":1:2:DOCUMENT_READ");

        when(redisTemplate.keys(pattern)).thenReturn(expectedKeys);

        // 执行方法
        Set<String> result = redisLearningRecordService.getLearningRecordKeys(date);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedKeys, result);
        verify(redisTemplate).keys(pattern);
    }

    @Test
    @DisplayName("获取学习记录数据")
    void testGetLearningRecordData() {
        // 准备测试数据
        String key = "learning:record:2023-06-01:1:2:VIDEO_WATCH";
        Map<Object, Object> expectedData = new HashMap<>();
        expectedData.put("totalDuration", 300);
        expectedData.put("lastUpdate", 1622505600000L);
        expectedData.put("chapterId", "3");
        expectedData.put("sectionId", "4");
        expectedData.put("contextData", "{\"progress\":75}");

        when(hashOperations.entries(key)).thenReturn(expectedData);

        // 执行方法
        Map<Object, Object> result = redisLearningRecordService.getLearningRecordData(key);

        // 验证结果
        assertNotNull(result);
        assertEquals(expectedData, result);
        verify(hashOperations).entries(key);
    }

    @Test
    @DisplayName("删除学习记录")
    void testDeleteLearningRecord() {
        // 准备测试数据
        String key = "learning:record:2023-06-01:1:2:VIDEO_WATCH";

        // 执行方法
        redisLearningRecordService.deleteLearningRecord(key);

        // 验证Redis操作
        verify(redisTemplate).delete(key);
    }
}
