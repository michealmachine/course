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

        // 聚合指定日期的学习记录
        aggregateLearningRecordsForDate(yesterday);
    }

    /**
     * 聚合指定日期的学习记录
     *
     * @param date 要聚合的日期
     */
    @Transactional
    public void aggregateLearningRecordsForDate(LocalDate date) {
        log.info("开始聚合 {} 的学习记录...", date);

        // 获取指定日期的所有学习记录键
        Set<String> keys = redisLearningRecordService.getLearningRecordKeys(date);
        log.info("找到 {} 个 {} 的学习记录键", keys.size(), date);

        int successCount = 0;
        int failureCount = 0;

        for (String key : keys) {
            try {
                // 解析键
                String[] parts = key.split(":");
                // 确保至少有基本的部分：learning:record:日期:用户ID:课程ID
                if (parts.length < 5) {
                    log.warn("无效的键格式: {}, 期望格式至少包含: learning:record:日期:用户ID:课程ID", key);
                    continue;
                }

                String dateStr = parts[2];
                Long userId;
                Long courseId;

                try {
                    userId = Long.parseLong(parts[3]);
                    courseId = Long.parseLong(parts[4]);
                } catch (NumberFormatException e) {
                    log.warn("无效的用户ID或课程ID格式: {}", key);
                    continue;
                }

                // 活动类型可能包含在键中，也可能存储在Redis数据中
                String activityType = parts.length > 5 ? parts[5] : "UNKNOWN";

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

                // 如果键中没有活动类型，尝试从Redis数据中获取
                if ("UNKNOWN".equals(activityType) && data.get("activityType") != null) {
                    activityType = data.get("activityType").toString();
                }

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
                LocalDate recordDate = LocalDate.parse(dateStr);
                LocalDateTime startTime = recordDate.atStartOfDay();
                LocalDateTime endTime = recordDate.atTime(23, 59, 59);

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
                LearningRecord savedRecord = learningRecordRepository.save(record);

                // 删除Redis键
                redisLearningRecordService.deleteLearningRecord(key);

                successCount++;
                log.info("成功聚合学习记录: {}, ID: {}, 用户: {}, 课程: {}, 活动类型: {}, 总时长: {}秒",
                        key, savedRecord.getId(), userId, courseId, activityType, totalDuration);
            } catch (Exception e) {
                failureCount++;
                log.error("处理键失败: " + key, e);
            }
        }

        log.info("{} 的学习记录聚合完成. 成功: {}, 失败: {}", date, successCount, failureCount);
    }

    /**
     * 手动触发聚合任务，处理当天和昨天的学习记录
     */
    @Transactional
    public void aggregateAllLearningRecords() {
        log.info("手动触发学习记录聚合任务...");

        // 聚合昨天的记录
        LocalDate yesterday = LocalDate.now().minusDays(1);
        aggregateLearningRecordsForDate(yesterday);

        // 聚合今天的记录
        LocalDate today = LocalDate.now();
        aggregateLearningRecordsForDate(today);

        log.info("手动聚合任务完成");
    }
}
