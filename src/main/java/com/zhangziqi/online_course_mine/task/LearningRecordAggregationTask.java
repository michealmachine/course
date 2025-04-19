package com.zhangziqi.online_course_mine.task;

import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.repository.ChapterRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.LearningRecordRepository;
import com.zhangziqi.online_course_mine.repository.SectionRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.RedisLearningRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 学习记录聚合定时任务
 * 定期将Redis中的学习记录聚合并保存到数据库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningRecordAggregationTask {
    
    private final RedisLearningRecordService redisLearningRecordService;
    private final LearningRecordRepository learningRecordRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SectionRepository sectionRepository;
    
    /**
     * 每天凌晨2点执行，聚合前一天的学习记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void aggregateLearningRecords() {
        log.info("开始聚合学习记录...");
        
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // 获取昨天的所有学习记录键
        Set<String> keys = redisLearningRecordService.getLearningRecordKeys(yesterday);
        log.info("找到 {} 个昨天的学习记录键", keys.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (String key : keys) {
            try {
                // 解析键
                String[] parts = key.split(":");
                if (parts.length != 5) {
                    log.warn("无效的键格式: {}", key);
                    continue;
                }
                
                String dateStr = parts[1];
                Long userId = Long.parseLong(parts[2]);
                Long courseId = Long.parseLong(parts[3]);
                String activityType = parts[4];
                
                // 获取数据
                Map<Object, Object> data = redisLearningRecordService.getLearningRecordData(key);
                
                if (data.isEmpty()) {
                    continue;
                }
                
                // 解析数据
                int totalDuration = ((Number) data.get("totalDuration")).intValue();
                Long chapterId = data.get("chapterId") != null ? Long.parseLong(data.get("chapterId").toString()) : null;
                Long sectionId = data.get("sectionId") != null ? Long.parseLong(data.get("sectionId").toString()) : null;
                String contextData = (String) data.get("contextData");
                
                // 查找实体
                User user = userRepository.findById(userId).orElse(null);
                Course course = courseRepository.findById(courseId).orElse(null);
                Chapter chapter = chapterId != null ? chapterRepository.findById(chapterId).orElse(null) : null;
                Section section = sectionId != null ? sectionRepository.findById(sectionId).orElse(null) : null;
                
                if (user == null || course == null) {
                    log.warn("找不到用户或课程: userId={}, courseId={}", userId, courseId);
                    continue;
                }
                
                // 创建学习记录
                LocalDate date = LocalDate.parse(dateStr);
                LocalDateTime startTime = date.atStartOfDay();
                LocalDateTime endTime = date.atTime(23, 59, 59);
                
                LearningRecord record = LearningRecord.builder()
                        .user(user)
                        .course(course)
                        .chapter(chapter)
                        .section(section)
                        .activityType(activityType)
                        .activityStartTime(startTime)
                        .activityEndTime(endTime)
                        .durationSeconds(totalDuration)
                        .contextData(contextData)
                        .build();
                
                // 保存到数据库
                learningRecordRepository.save(record);
                
                // 删除Redis键
                redisLearningRecordService.deleteLearningRecord(key);
                
                successCount++;
                log.debug("成功聚合学习记录: {}, 总时长: {}秒", key, totalDuration);
            } catch (Exception e) {
                failureCount++;
                log.error("处理键失败: " + key, e);
            }
        }
        
        log.info("学习记录聚合完成. 成功: {}, 失败: {}", successCount, failureCount);
    }
}
