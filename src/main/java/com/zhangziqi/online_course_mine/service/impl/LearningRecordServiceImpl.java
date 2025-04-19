package com.zhangziqi.online_course_mine.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordCompletedDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordEndDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordStartDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.DateLearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningRecordVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.LearningRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 学习记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl implements LearningRecordService {

    private final LearningRecordRepository learningRecordRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SectionRepository sectionRepository;
    private final UserCourseRepository userCourseRepository;
    private final ObjectMapper objectMapper;
    private final RedisLearningRecordService redisLearningRecordService;

    @Override
    @Transactional
    public LearningRecordVO startActivity(Long userId, LearningRecordStartDTO dto) {
        log.info("开始学习活动, 用户ID: {}, 课程ID: {}, 章节ID: {}, 小节ID: {}, 活动类型: {}",
                userId, dto.getCourseId(), dto.getChapterId(), dto.getSectionId(), dto.getActivityType());

        // 验证用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 验证课程
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 验证用户是否已购买课程，并且课程状态为正常（未退款、未过期）
        if (!userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(userId, dto.getCourseId(),
                UserCourseStatus.NORMAL.getValue())) {
            throw new BusinessException(403, "请先购买课程再进行学习，或检查课程是否已过期或退款");
        }

        // 如果有正在进行的活动，先结束它
        Optional<LearningRecord> ongoingActivity = learningRecordRepository.findByUser_IdAndActivityEndTimeIsNull(userId);
        if (ongoingActivity.isPresent()) {
            LearningRecord ongoing = ongoingActivity.get();
            ongoing.setActivityEndTime(LocalDateTime.now());
            ongoing.setDurationSeconds((int) (ongoing.getActivityEndTime().toEpochSecond(ZoneOffset.UTC) -
                    ongoing.getActivityStartTime().toEpochSecond(ZoneOffset.UTC)));
            learningRecordRepository.save(ongoing);
            log.info("自动结束之前未完成的学习活动, ID: {}", ongoing.getId());
        }

        // 查找章节和小节（如果有）
        Chapter chapter = null;
        if (dto.getChapterId() != null) {
            chapter = chapterRepository.findById(dto.getChapterId()).orElse(null);
        }

        Section section = null;
        if (dto.getSectionId() != null) {
            section = sectionRepository.findById(dto.getSectionId()).orElse(null);
        }

        // 验证活动类型是否有效
        LearningActivityType activityType = LearningActivityType.getByCode(dto.getActivityType());
        if (activityType == null) {
            throw new BusinessException(400, "无效的活动类型");
        }

        // 创建学习记录
        LearningRecord record = LearningRecord.builder()
                .user(user)
                .course(course)
                .chapter(chapter)
                .section(section)
                .activityType(dto.getActivityType())
                .activityStartTime(LocalDateTime.now())
                .contextData(dto.getContextData())
                .build();

        LearningRecord savedRecord = learningRecordRepository.save(record);
        log.info("成功创建学习活动记录, ID: {}", savedRecord.getId());

        return LearningRecordVO.fromEntity(savedRecord);
    }

    @Override
    @Transactional
    public LearningRecordVO endActivity(Long userId, Long recordId, LearningRecordEndDTO dto) {
        log.info("结束学习活动, 用户ID: {}, 记录ID: {}", userId, recordId);

        // 获取学习记录
        LearningRecord record = learningRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("学习记录不存在"));

        // 验证所有权
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此学习记录");
        }

        // 验证记录是否已结束
        if (record.getActivityEndTime() != null) {
            throw new BusinessException(400, "该学习活动已结束");
        }

        // 设置结束时间和持续时长
        record.setActivityEndTime(LocalDateTime.now());
        record.setDurationSeconds((int) (record.getActivityEndTime().toEpochSecond(ZoneOffset.UTC) -
                record.getActivityStartTime().toEpochSecond(ZoneOffset.UTC)));

        // 更新上下文数据（如果有）
        if (dto.getContextData() != null && !dto.getContextData().isEmpty()) {
            record.setContextData(dto.getContextData());
        }

        LearningRecord updatedRecord = learningRecordRepository.save(record);
        log.info("成功结束学习活动, ID: {}, 持续时间: {}秒", updatedRecord.getId(), updatedRecord.getDurationSeconds());

        return LearningRecordVO.fromEntity(updatedRecord);
    }

    @Override
    @Transactional
    public LearningRecordVO recordCompletedActivity(Long userId, LearningRecordCompletedDTO dto) {
        log.info("记录已完成学习活动, 用户ID: {}, 课程ID: {}, 章节ID: {}, 小节ID: {}, 活动类型: {}",
                userId, dto.getCourseId(), dto.getChapterId(), dto.getSectionId(), dto.getActivityType());

        // 验证用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 验证课程
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 验证用户是否已购买课程，并且课程状态为正常（未退款、未过期）
        if (!userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(userId, dto.getCourseId(),
                UserCourseStatus.NORMAL.getValue())) {
            throw new BusinessException(403, "请先购买课程再进行学习，或检查课程是否已过期或退款");
        }

        // 查找章节和小节（如果有）
        Chapter chapter = null;
        if (dto.getChapterId() != null) {
            chapter = chapterRepository.findById(dto.getChapterId()).orElse(null);
        }

        Section section = null;
        if (dto.getSectionId() != null) {
            section = sectionRepository.findById(dto.getSectionId()).orElse(null);
        }

        // 验证活动类型是否有效
        LearningActivityType activityType = LearningActivityType.getByCode(dto.getActivityType());
        if (activityType == null) {
            throw new BusinessException(400, "无效的活动类型");
        }

        // 验证持续时间是否有效
        if (dto.getDurationSeconds() == null || dto.getDurationSeconds() <= 0) {
            throw new BusinessException(400, "持续时间必须大于0");
        }

        // 使用Redis服务存储学习记录
        redisLearningRecordService.updateLearningRecord(
                userId,
                dto.getCourseId(),
                dto.getChapterId(),
                dto.getSectionId(),
                dto.getActivityType(),
                dto.getDurationSeconds(),
                dto.getContextData()
        );

        // 构建返回对象
        LearningRecordVO vo = new LearningRecordVO();
        vo.setUserId(userId);
        vo.setCourseId(dto.getCourseId());
        vo.setChapterId(dto.getChapterId());
        vo.setSectionId(dto.getSectionId());
        vo.setActivityType(dto.getActivityType());
        vo.setActivityTypeDescription(activityType.getDescription());
        vo.setDurationSeconds(dto.getDurationSeconds());
        vo.setContextData(dto.getContextData());

        // 设置课程、章节、小节标题
        vo.setCourseTitle(course.getTitle());
        if (chapter != null) {
            vo.setChapterTitle(chapter.getTitle());
        }
        if (section != null) {
            vo.setSectionTitle(section.getTitle());
        }

        // 设置活动时间
        LocalDateTime now = LocalDateTime.now();
        vo.setActivityStartTime(now.minusSeconds(dto.getDurationSeconds()));
        vo.setActivityEndTime(now);

        log.info("成功记录已完成学习活动到Redis, 用户ID: {}, 课程ID: {}, 活动类型: {}, 持续时间: {}秒",
                userId, dto.getCourseId(), dto.getActivityType(), dto.getDurationSeconds());

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LearningRecordVO> findOngoingActivity(Long userId) {
        log.info("查找用户当前进行中的学习活动, 用户ID: {}", userId);

        Optional<LearningRecord> record = learningRecordRepository.findByUser_IdAndActivityEndTimeIsNull(userId);
        return record.map(LearningRecordVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningRecordVO> getUserActivities(Long userId, Pageable pageable) {
        log.info("获取用户学习记录(分页), 用户ID: {}, 页码: {}, 每页数量: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<LearningRecord> records = learningRecordRepository.findByUser_IdOrderByActivityStartTimeDesc(userId, pageable);
        return records.map(LearningRecordVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningRecordVO> getUserCourseActivities(Long userId, Long courseId, Pageable pageable) {
        log.info("获取用户课程学习记录(分页), 用户ID: {}, 课程ID: {}, 页码: {}, 每页数量: {}",
                userId, courseId, pageable.getPageNumber(), pageable.getPageSize());

        Page<LearningRecord> records = learningRecordRepository.findByUser_IdAndCourse_IdOrderByActivityStartTimeDesc(
                userId, courseId, pageable);
        return records.map(LearningRecordVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyLearningStatVO> getDailyLearningStats(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户每日学习统计, 用户ID: {}, 开始日期: {}, 结束日期: {}", userId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = learningRecordRepository.findDailyLearningDurationByUserId(
                userId, startDateTime, endDateTime);

        List<DailyLearningStatVO> stats = new ArrayList<>();
        for (Object[] result : results) {
            String date = (String) result[0];
            Long duration = result[1] != null ? ((Number) result[1]).longValue() : 0L;
            Integer count = result[2] != null ? ((Number) result[2]).intValue() : 0;

            stats.add(DailyLearningStatVO.builder()
                    .date(date)
                    .durationSeconds(duration)
                    .activityCount(count)
                    .build());
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityTypeStatVO> getActivityTypeStats(Long userId) {
        log.info("获取用户活动类型统计, 用户ID: {}", userId);

        List<Object[]> results = learningRecordRepository.findLearningDurationByActivityType(userId);

        List<ActivityTypeStatVO> stats = new ArrayList<>();
        for (Object[] result : results) {
            String activityType = (String) result[0];
            Long duration = result[1] != null ? ((Number) result[1]).longValue() : 0L;
            Integer count = result[2] != null ? ((Number) result[2]).intValue() : 0;

            LearningActivityType type = LearningActivityType.getByCode(activityType);
            String description = type != null ? type.getDescription() : activityType;

            stats.add(ActivityTypeStatVO.builder()
                    .activityType(activityType)
                    .activityTypeDescription(description)
                    .totalDurationSeconds(duration)
                    .activityCount(count)
                    .build());
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTodayLearningDuration(Long userId) {
        log.info("获取用户今日学习时长, 用户ID: {}", userId);

        Long duration = learningRecordRepository.findTodayLearningDuration(userId);
        return duration != null ? duration : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalLearningDuration(Long userId) {
        log.info("获取用户总学习时长, 用户ID: {}", userId);

        List<LearningRecord> records = learningRecordRepository.findByUser_Id(userId);
        return records.stream()
                .filter(r -> r.getDurationSeconds() != null)
                .mapToLong(LearningRecord::getDurationSeconds)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public LearningHeatmapVO getUserLearningHeatmap(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户学习热力图数据, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                userId, startDate, endDate);

        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询热力图数据
        List<Object[]> results = learningRecordRepository.findLearningHeatmapDataByUser(
                userId, startDateTime, endDateTime);

        // 处理查询结果
        Map<Integer, Map<Integer, Integer>> heatmapData = new HashMap<>();
        int maxCount = 0;

        for (Object[] result : results) {
            int weekday = ((Number) result[0]).intValue();
            int hour = ((Number) result[1]).intValue();
            int count = ((Number) result[2]).intValue();

            // 更新最大活动次数
            if (count > maxCount) {
                maxCount = count;
            }

            // 更新热力图数据
            heatmapData.computeIfAbsent(weekday, k -> new HashMap<>())
                    .put(hour, count);
        }

        return LearningHeatmapVO.builder()
                .courseId(null) // 用户总体热力图没有特定课程
                .heatmapData(heatmapData)
                .maxActivityCount(maxCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DateLearningHeatmapVO getUserLearningHeatmapByDate(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户按日期分组的学习热力图数据, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                userId, startDate, endDate);

        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询按日期分组的热力图数据
        List<Object[]> results = learningRecordRepository.findLearningHeatmapDataByUserGroupByDate(
                userId, startDateTime, endDateTime);

        // 处理查询结果
        Map<String, Integer> heatmapData = new HashMap<>();
        int maxCount = 0;

        for (Object[] result : results) {
            String date = (String) result[0];
            int count = ((Number) result[1]).intValue();

            // 更新最大活动次数
            if (count > maxCount) {
                maxCount = count;
            }

            // 更新热力图数据
            heatmapData.put(date, count);
        }

        return DateLearningHeatmapVO.builder()
                .courseId(null) // 用户总体热力图没有特定课程
                .heatmapData(heatmapData)
                .maxActivityCount(maxCount)
                .build();
    }
}