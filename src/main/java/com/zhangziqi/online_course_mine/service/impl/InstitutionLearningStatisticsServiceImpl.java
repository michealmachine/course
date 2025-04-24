package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.config.CacheConfig;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionLearningStatisticsVO;
import com.zhangziqi.online_course_mine.model.vo.LearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningProgressTrendVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.LearningRecordRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.service.InstitutionLearningStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 机构学习统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionLearningStatisticsServiceImpl implements InstitutionLearningStatisticsService {

    private final InstitutionRepository institutionRepository;
    private final CourseRepository courseRepository;
    private final LearningRecordRepository learningRecordRepository;
    private final UserCourseRepository userCourseRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE, key = "'institution_statistics_' + #institutionId")
    public InstitutionLearningStatisticsVO getInstitutionLearningStatistics(Long institutionId) {
        log.info("获取机构学习统计, 机构ID: {}", institutionId);

        // 验证机构存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 创建统计VO
        InstitutionLearningStatisticsVO statisticsVO = InstitutionLearningStatisticsVO.builder()
                .institutionId(institutionId)
                .institutionName(institution.getName())
                .build();

        // 获取总学习人数
        Long totalLearners = learningRecordRepository.countUniqueUsersByInstitution(institutionId);
        statisticsVO.setTotalLearners(totalLearners != null ? totalLearners : 0L);

        // 获取总学习时长
        Long totalDuration = learningRecordRepository.findTotalLearningDurationByInstitution(institutionId);
        statisticsVO.setTotalLearningDuration(totalDuration != null ? totalDuration : 0L);

        // 获取今日学习时长
        Long todayDuration = learningRecordRepository.findTodayLearningDurationByInstitution(institutionId);
        statisticsVO.setTodayLearningDuration(todayDuration != null ? todayDuration : 0L);

        // 计算本周学习时长
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfWeek = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        LocalDateTime weekStart = firstDayOfWeek.atStartOfDay();
        LocalDateTime weekEnd = today.atTime(LocalTime.MAX);

        List<LearningRecord> weekRecords = learningRecordRepository.findByInstitutionIdAndTimeRange(
                institutionId, weekStart, weekEnd);
        Long weekDuration = weekRecords.stream()
                .filter(lr -> lr.getDurationSeconds() != null)
                .mapToLong(LearningRecord::getDurationSeconds)
                .sum();
        statisticsVO.setWeekLearningDuration(weekDuration);

        // 计算本月学习时长
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
        LocalDateTime monthEnd = today.atTime(LocalTime.MAX);

        List<LearningRecord> monthRecords = learningRecordRepository.findByInstitutionIdAndTimeRange(
                institutionId, monthStart, monthEnd);
        Long monthDuration = monthRecords.stream()
                .filter(lr -> lr.getDurationSeconds() != null)
                .mapToLong(LearningRecord::getDurationSeconds)
                .sum();
        statisticsVO.setMonthLearningDuration(monthDuration);

        // 计算活跃课程数
        List<Object[]> courseStats = learningRecordRepository.findLearningStatsByCourseForInstitution(institutionId);
        statisticsVO.setTotalActiveCourses(courseStats.size());

        // 获取题目尝试次数
        long quizAttempts = weekRecords.stream()
                .filter(lr -> LearningActivityType.QUIZ_ATTEMPT.getCode().equals(lr.getActivityType()))
                .count();
        statisticsVO.setTotalQuestionAttempts((int) quizAttempts);

        // 获取每日学习统计
        LocalDate startDate = today.minusDays(29);
        List<DailyLearningStatVO> dailyStats = getInstitutionDailyLearningStats(
                institutionId, startDate, today);
        statisticsVO.setDailyLearning(dailyStats);

        // 获取活动类型统计
        List<ActivityTypeStatVO> activityTypeStats = getInstitutionActivityTypeStats(institutionId);
        statisticsVO.setActivityTypeStats(activityTypeStats);

        // 获取最活跃用户
        List<InstitutionLearningStatisticsVO.ActiveUserVO> activeUsers = getMostActiveUsers(institutionId, 10);
        statisticsVO.setMostActiveUsers(activeUsers);

        // 获取课程统计（仅前5个）
        Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> courseStatisticsPage =
                getInstitutionCourseStatistics(institutionId, PageRequest.of(0, 5));
        statisticsVO.setCourseStatistics(courseStatisticsPage.getContent());

        log.info("成功获取机构学习统计, 机构ID: {}, 学习人数: {}, 学习时长: {}秒",
                institutionId, totalLearners, totalDuration);

        return statisticsVO;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE,
              key = "'institution_daily_stats_' + #institutionId + '_' + #startDate + '_' + #endDate")
    public List<DailyLearningStatVO> getInstitutionDailyLearningStats(
            Long institutionId, LocalDate startDate, LocalDate endDate) {
        log.info("获取机构每日学习统计, 机构ID: {}, 开始日期: {}, 结束日期: {}",
                institutionId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = learningRecordRepository.findDailyLearningStatsByInstitutionId(
                institutionId, startDateTime, endDateTime);

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
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE,
              key = "'institution_activity_type_stats_' + #institutionId")
    public List<ActivityTypeStatVO> getInstitutionActivityTypeStats(Long institutionId) {
        log.info("获取机构活动类型统计, 机构ID: {}", institutionId);

        List<Object[]> results = learningRecordRepository.findLearningStatsByActivityTypeForInstitution(institutionId);

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
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE,
              key = "'institution_course_stats_' + #institutionId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> getInstitutionCourseStatistics(
            Long institutionId, Pageable pageable) {
        log.info("获取机构课程学习统计, 机构ID: {}, 页码: {}, 每页数量: {}",
                institutionId, pageable.getPageNumber(), pageable.getPageSize());

        // 获取机构课程
        List<Course> courses = courseRepository.findByInstitution(
                Institution.builder().id(institutionId).build(), Pageable.unpaged()).getContent();

        if (courses.isEmpty()) {
            return Page.empty(pageable);
        }

        // 课程ID与课程映射
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        // 获取课程学习统计
        List<Object[]> courseStatsRaw = learningRecordRepository.findLearningStatsByCourseForInstitution(institutionId);

        // 转换为VO
        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> allCourseStats = new ArrayList<>();

        for (Object[] rawStat : courseStatsRaw) {
            Long courseId = ((Number) rawStat[0]).longValue();
            String courseTitle = (String) rawStat[1];
            Long totalDuration = rawStat[2] != null ? ((Number) rawStat[2]).longValue() : 0L;
            Integer activityCount = rawStat[3] != null ? ((Number) rawStat[3]).intValue() : 0;

            // 查询课程学习人数
            List<Object[]> learnerCountQuery = userCourseRepository.countLearnersByCourseId(courseId);
            Long learnerCount = learnerCountQuery.isEmpty() ? 0L :
                    (learnerCountQuery.get(0)[0] instanceof Number ? ((Number) learnerCountQuery.get(0)[0]).longValue() : 0L);

            // 查询完成的人数
            Long completionCount = userCourseRepository.countByProgress(courseId, 100);

            // 计算平均进度
            Double averageProgress = userCourseRepository.getAverageProgressByCourseId(courseId);
            if (averageProgress == null) {
                averageProgress = 0.0;
            }

            InstitutionLearningStatisticsVO.CourseStatisticsVO stat =
                    InstitutionLearningStatisticsVO.CourseStatisticsVO.builder()
                    .courseId(courseId)
                    .courseTitle(courseTitle)
                    .totalDuration(totalDuration)
                    .activityCount(activityCount)
                    .learnerCount(learnerCount)
                    .completionCount(completionCount)
                    .averageProgress(averageProgress)
                    .build();

            allCourseStats.add(stat);
        }

        // 分页处理
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allCourseStats.size());

        if (start > allCourseStats.size()) {
            return Page.empty(pageable);
        }

        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> pagedResults =
                allCourseStats.subList(start, end);

        return new PageImpl<>(pagedResults, pageable, allCourseStats.size());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE,
              key = "'institution_active_users_' + #institutionId + '_limit_' + #limit")
    public List<InstitutionLearningStatisticsVO.ActiveUserVO> getMostActiveUsers(Long institutionId, int limit) {
        log.info("获取机构最活跃用户, 机构ID: {}, 限制: {}", institutionId, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = learningRecordRepository.findMostActiveUsersByInstitution(institutionId, pageable);

        List<InstitutionLearningStatisticsVO.ActiveUserVO> activeUsers = new ArrayList<>();
        for (Object[] result : results) {
            Long userId = ((Number) result[0]).longValue();
            String username = (String) result[1];
            Long duration = result[2] != null ? ((Number) result[2]).longValue() : 0L;
            Integer count = result[3] != null ? ((Number) result[3]).intValue() : 0;

            activeUsers.add(InstitutionLearningStatisticsVO.ActiveUserVO.builder()
                    .userId(userId)
                    .username(username)
                    .learningDuration(duration)
                    .activityCount(count)
                    .build());
        }

        return activeUsers;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE,
              key = "'institution_today_duration_' + #institutionId")
    public Long getInstitutionTodayLearningDuration(Long institutionId) {
        log.info("获取机构今日学习时长, 机构ID: {}", institutionId);

        Long duration = learningRecordRepository.findTodayLearningDurationByInstitution(institutionId);
        return duration != null ? duration : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE,
              key = "'institution_total_duration_' + #institutionId")
    public Long getInstitutionTotalLearningDuration(Long institutionId) {
        log.info("获取机构总学习时长, 机构ID: {}", institutionId);

        Long duration = learningRecordRepository.findTotalLearningDurationByInstitution(institutionId);
        return duration != null ? duration : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.INSTITUTION_STATS_CACHE,
              key = "'institution_learner_count_' + #institutionId")
    public Long getInstitutionLearnerCount(Long institutionId) {
        log.info("获取机构学习人数, 机构ID: {}", institutionId);

        Long count = learningRecordRepository.countUniqueUsersByInstitution(institutionId);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_statistics_' + #institutionId + '_' + #courseId")
    public InstitutionLearningStatisticsVO.CourseStatisticsVO getCourseLearningStatistics(Long institutionId, Long courseId) {
        log.info("获取课程学习统计, 机构ID: {}, 课程ID: {}", institutionId, courseId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        // 查询课程学习时长
        Long totalDuration = learningRecordRepository.findTotalLearningDurationByCourse(courseId);

        // 查询课程学习活动次数
        List<Object[]> statResults = learningRecordRepository.findLearningStatsByCourseForInstitution(institutionId)
                .stream()
                .filter(result -> courseId.equals(((Number) result[0]).longValue()))
                .collect(Collectors.toList());

        Integer activityCount = 0;
        if (!statResults.isEmpty()) {
            activityCount = ((Number) statResults.get(0)[3]).intValue();
        }

        // 查询课程学习人数
        List<Object[]> learnerCountQuery = userCourseRepository.countLearnersByCourseId(courseId);
        Long learnerCount = learnerCountQuery.isEmpty() ? 0L :
                (learnerCountQuery.get(0)[0] instanceof Number ? ((Number) learnerCountQuery.get(0)[0]).longValue() : 0L);

        // 查询完成人数
        Long completionCount = userCourseRepository.countByProgress(courseId, 100);

        // 计算平均进度
        Double averageProgress = userCourseRepository.getAverageProgressByCourseId(courseId);
        if (averageProgress == null) {
            averageProgress = 0.0;
        }

        // 构建返回对象
        return InstitutionLearningStatisticsVO.CourseStatisticsVO.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .totalDuration(totalDuration != null ? totalDuration : 0L)
                .activityCount(activityCount)
                .learnerCount(learnerCount)
                .completionCount(completionCount != null ? completionCount : 0L)
                .averageProgress(averageProgress)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_daily_stats_' + #institutionId + '_' + #courseId + '_' + #startDate + '_' + #endDate")
    public List<DailyLearningStatVO> getCourseDailyLearningStats(
            Long institutionId, Long courseId, LocalDate startDate, LocalDate endDate) {
        log.info("获取课程每日学习统计, 机构ID: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                institutionId, courseId, startDate, endDate);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = learningRecordRepository.findDailyLearningStatsByCourseId(
                courseId, startDateTime, endDateTime);

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
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_activity_type_stats_' + #institutionId + '_' + #courseId")
    public List<ActivityTypeStatVO> getCourseActivityTypeStats(Long institutionId, Long courseId) {
        log.info("获取课程活动类型统计, 机构ID: {}, 课程ID: {}", institutionId, courseId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        List<Object[]> results = learningRecordRepository.findLearningStatsByActivityTypeForCourse(courseId);

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
    // 暂时禁用缓存以便调试
    // @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
    //           key = "'course_student_stats_' + #institutionId + '_' + #courseId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<InstitutionLearningStatisticsVO.StudentLearningVO> getCourseStudentStatistics(
            Long institutionId, Long courseId, Pageable pageable) {
        log.info("获取课程学生学习统计, 机构ID: {}, 课程ID: {}, 页码: {}, 每页数量: {}",
                institutionId, courseId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            // 验证机构存在
            institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

            // 验证课程存在且属于该机构
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

            log.info("课程信息: ID={}, 标题={}, 机构ID={}", course.getId(), course.getTitle(), course.getInstitutionId());

            if (!institutionId.equals(course.getInstitutionId())) {
                throw new ResourceNotFoundException("该课程不属于指定机构");
            }

            // 获取课程学生学习统计
            List<Object[]> studentStats = learningRecordRepository.findStudentLearningStatsByCourse(courseId, pageable);
            log.info("查询到的学生学习统计数据条数: {}", studentStats.size());

            List<InstitutionLearningStatisticsVO.StudentLearningVO> studentLearningList = new ArrayList<>();
            for (Object[] stat : studentStats) {
                Long userId = ((Number) stat[0]).longValue();
                String username = (String) stat[1];
                Long duration = stat[2] != null ? ((Number) stat[2]).longValue() : 0L;
                Integer count = stat[3] != null ? ((Number) stat[3]).intValue() : 0;
                LocalDateTime lastLearnTime = (LocalDateTime) stat[4];

                log.info("学生学习统计数据: 用户ID={}, 用户名={}, 学习时长={}, 活动次数={}, 最后学习时间={}",
                        userId, username, duration, count, lastLearnTime);

                // 获取用户课程学习进度
                Integer progress = userCourseRepository.findProgressByUserIdAndCourseId(userId, courseId);
                log.info("用户课程学习进度: {}", progress);

                studentLearningList.add(InstitutionLearningStatisticsVO.StudentLearningVO.builder()
                        .userId(userId)
                        .username(username)
                        .learningDuration(duration)
                        .activityCount(count)
                        .progress(progress != null ? progress : 0)
                        .lastLearnTime(lastLearnTime)
                        .build());
            }

            // 创建分页返回对象
            long total = learningRecordRepository.countUniqueUsersByCourse(courseId);
            log.info("课程学习人数总计: {}", total);

            Page<InstitutionLearningStatisticsVO.StudentLearningVO> result =
                new PageImpl<>(studentLearningList, pageable, total);
            log.info("返回的分页数据: 内容大小={}, 总页数={}, 总元素数={}",
                result.getContent().size(), result.getTotalPages(), result.getTotalElements());

            return result;
        } catch (Exception e) {
            log.error("获取课程学生学习统计失败", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_today_duration_' + #institutionId + '_' + #courseId")
    public Long getCourseTodayLearningDuration(Long institutionId, Long courseId) {
        log.info("获取课程今日学习时长, 机构ID: {}, 课程ID: {}", institutionId, courseId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        Long duration = learningRecordRepository.findTodayLearningDurationByCourse(courseId);
        return duration != null ? duration : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_total_duration_' + #institutionId + '_' + #courseId")
    public Long getCourseTotalLearningDuration(Long institutionId, Long courseId) {
        log.info("获取课程总学习时长, 机构ID: {}, 课程ID: {}", institutionId, courseId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        Long duration = learningRecordRepository.findTotalLearningDurationByCourse(courseId);
        return duration != null ? duration : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_learner_count_' + #institutionId + '_' + #courseId")
    public Long getCourseLearnerCount(Long institutionId, Long courseId) {
        log.info("获取课程学习人数, 机构ID: {}, 课程ID: {}", institutionId, courseId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        Long count = learningRecordRepository.countUniqueUsersByCourse(courseId);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_heatmap_' + #institutionId + '_' + #courseId + '_' + #startDate + '_' + #endDate")
    public LearningHeatmapVO getCourseLearningHeatmap(Long institutionId, Long courseId, LocalDate startDate, LocalDate endDate) {
        log.info("获取课程学习热力图数据, 机构ID: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                institutionId, courseId, startDate, endDate);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询热力图数据
        List<Object[]> results = learningRecordRepository.findLearningHeatmapDataByCourse(
                courseId, startDateTime, endDateTime);

        // 处理查询结果
        Map<Integer, Map<Integer, Integer>> heatmapData = new HashMap<>();
        int maxDuration = 0;

        for (Object[] result : results) {
            int weekday = ((Number) result[0]).intValue();
            int hour = ((Number) result[1]).intValue();
            int duration = result[2] != null ? ((Number) result[2]).intValue() : 0;

            // 更新最大学习时长
            if (duration > maxDuration) {
                maxDuration = duration;
            }

            // 更新热力图数据
            heatmapData.computeIfAbsent(weekday, k -> new HashMap<>())
                    .put(hour, duration);
        }

        return LearningHeatmapVO.builder()
                .courseId(courseId)
                .heatmapData(heatmapData)
                .maxActivityCount(maxDuration) // 字段名保持不变，但实际存储的是最大学习时长
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'course_progress_trend_' + #institutionId + '_' + #courseId + '_' + #startDate + '_' + #endDate")
    public LearningProgressTrendVO getCourseLearningProgressTrend(Long institutionId, Long courseId, LocalDate startDate, LocalDate endDate) {
        log.info("获取课程学习进度趋势, 机构ID: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                institutionId, courseId, startDate, endDate);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询进度趋势数据
        List<Object[]> results = userCourseRepository.findDailyProgressTrendByCourse(
                courseId, startDateTime, endDateTime);

        // 处理查询结果
        List<LearningProgressTrendVO.DailyProgressVO> progressData = new ArrayList<>();

        for (Object[] result : results) {
            String date = (String) result[0];
            Double avgProgress = ((Number) result[1]).doubleValue();
            Integer userCount = ((Number) result[2]).intValue();

            progressData.add(LearningProgressTrendVO.DailyProgressVO.builder()
                    .date(date)
                    .averageProgress(avgProgress)
                    .activeUserCount(userCount)
                    .build());
        }

        return LearningProgressTrendVO.builder()
                .courseId(courseId)
                .progressData(progressData)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'user_course_heatmap_' + #institutionId + '_' + #courseId + '_' + #userId + '_' + #startDate + '_' + #endDate")
    public LearningHeatmapVO getUserCourseLearningHeatmap(Long institutionId, Long courseId, Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户课程学习热力图数据, 机构ID: {}, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                institutionId, courseId, userId, startDate, endDate);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询用户课程热力图数据
        List<Object[]> results = learningRecordRepository.findLearningHeatmapDataByUserAndCourse(
                userId, courseId, startDateTime, endDateTime);

        // 处理查询结果
        Map<Integer, Map<Integer, Integer>> heatmapData = new HashMap<>();
        int maxDuration = 0;

        for (Object[] result : results) {
            int weekday = ((Number) result[0]).intValue();
            int hour = ((Number) result[1]).intValue();
            int duration = result[2] != null ? ((Number) result[2]).intValue() : 0;

            // 更新最大学习时长
            if (duration > maxDuration) {
                maxDuration = duration;
            }

            // 更新热力图数据
            heatmapData.computeIfAbsent(weekday, k -> new HashMap<>())
                    .put(hour, duration);
        }

        LearningHeatmapVO heatmapVO = LearningHeatmapVO.builder()
                .courseId(courseId)
                .heatmapData(heatmapData)
                .maxActivityCount(maxDuration) // 字段名保持不变，但实际存储的是最大学习时长
                .build();
        // 注意：LearningHeatmapVO类中没有userId字段，所以不能设置
        return heatmapVO;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'user_course_progress_trend_' + #institutionId + '_' + #courseId + '_' + #userId + '_' + #startDate + '_' + #endDate")
    public LearningProgressTrendVO getUserCourseLearningProgressTrend(Long institutionId, Long courseId, Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户课程学习进度趋势, 机构ID: {}, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                institutionId, courseId, userId, startDate, endDate);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询用户课程进度趋势数据
        List<Object[]> results = userCourseRepository.findDailyProgressTrendByUserAndCourse(
                userId, courseId, startDateTime, endDateTime);

        // 处理查询结果
        List<LearningProgressTrendVO.DailyProgressVO> progressData = new ArrayList<>();

        for (Object[] result : results) {
            String date = (String) result[0];
            Double progress = ((Number) result[1]).doubleValue();
            Integer activityCount = ((Number) result[2]).intValue();

            progressData.add(LearningProgressTrendVO.DailyProgressVO.builder()
                    .date(date)
                    .averageProgress(progress)
                    .activeUserCount(activityCount)
                    .build());
        }

        LearningProgressTrendVO trendVO = LearningProgressTrendVO.builder()
                .courseId(courseId)
                .progressData(progressData)
                .build();
        // 注意：LearningProgressTrendVO类中没有userId字段，所以不能设置
        return trendVO;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'user_course_activity_type_stats_' + #institutionId + '_' + #courseId + '_' + #userId")
    public List<ActivityTypeStatVO> getUserCourseActivityTypeStats(Long institutionId, Long courseId, Long userId) {
        log.info("获取用户课程活动类型统计, 机构ID: {}, 课程ID: {}, 用户ID: {}",
                institutionId, courseId, userId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        // 查询用户课程活动类型统计
        List<Object[]> results = learningRecordRepository.findLearningStatsByActivityTypeForUserAndCourse(
                userId, courseId);

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
    @Cacheable(value = CacheConfig.COURSE_STATS_CACHE,
              key = "'user_course_learning_detail_' + #institutionId + '_' + #courseId + '_' + #userId")
    public InstitutionLearningStatisticsVO.StudentLearningDetailVO getUserCourseLearningDetail(Long institutionId, Long courseId, Long userId) {
        log.info("获取用户课程学习详情, 机构ID: {}, 课程ID: {}, 用户ID: {}",
                institutionId, courseId, userId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 验证课程存在且属于该机构
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        if (!institutionId.equals(course.getInstitutionId())) {
            throw new ResourceNotFoundException("该课程不属于指定机构");
        }

        // 查询用户基本信息
        Object[] userBasicInfo = learningRecordRepository.findUserBasicInfoByUserIdAndCourseId(userId, courseId);
        if (userBasicInfo == null || userBasicInfo.length == 0) {
            throw new ResourceNotFoundException("未找到用户学习记录");
        }

        String username = (String) userBasicInfo[0];
        Long learningDuration = userBasicInfo[1] != null ? ((Number) userBasicInfo[1]).longValue() : 0L;
        Integer activityCount = userBasicInfo[2] != null ? ((Number) userBasicInfo[2]).intValue() : 0;
        LocalDateTime lastLearnTime = (LocalDateTime) userBasicInfo[3];

        // 获取用户课程学习进度
        Integer progress = userCourseRepository.findProgressByUserIdAndCourseId(userId, courseId);

        // 获取课程章节和小节信息
        Object[] courseStructureInfo = courseRepository.findCourseStructureInfo(courseId);
        Integer totalChapters = courseStructureInfo[0] != null ? ((Number) courseStructureInfo[0]).intValue() : 0;
        Integer totalSections = courseStructureInfo[1] != null ? ((Number) courseStructureInfo[1]).intValue() : 0;

        // 获取用户完成的章节和小节数
        Object[] completionInfo = learningRecordRepository.findUserCourseCompletionInfo(userId, courseId);
        Integer completedChapters = completionInfo[0] != null ? ((Number) completionInfo[0]).intValue() : 0;
        Integer completedSections = completionInfo[1] != null ? ((Number) completionInfo[1]).intValue() : 0;

        // 获取用户测验完成情况
        Object[] quizInfo = learningRecordRepository.findUserCourseQuizInfo(userId, courseId);
        Integer completedQuizzes = quizInfo[0] != null ? ((Number) quizInfo[0]).intValue() : 0;
        Double quizAccuracy = quizInfo[1] != null ? ((Number) quizInfo[1]).doubleValue() : 0.0;

        // 构建返回对象
        return InstitutionLearningStatisticsVO.StudentLearningDetailVO.builder()
                .userId(userId)
                .username(username)
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .learningDuration(learningDuration)
                .progress(progress != null ? progress : 0)
                .activityCount(activityCount)
                .lastLearnTime(lastLearnTime)
                .completedChapters(completedChapters)
                .totalChapters(totalChapters)
                .completedSections(completedSections)
                .totalSections(totalSections)
                .completedQuizzes(completedQuizzes)
                .quizAccuracy(quizAccuracy)
                .build();
    }
}