package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.config.CacheConfig;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
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
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.AdminLearningStatisticsService;
import com.zhangziqi.online_course_mine.service.InstitutionLearningStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员学习统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLearningStatisticsServiceImpl implements AdminLearningStatisticsService {

    private final CourseRepository courseRepository;
    private final LearningRecordRepository learningRecordRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionLearningStatisticsService institutionLearningStatisticsService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'all_course_stats_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> getAllCourseStatistics(Pageable pageable) {
        log.info("获取所有课程学习统计, 页码: {}, 每页数量: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // 获取所有课程
        List<Course> courses = courseRepository.findAll();

        if (courses.isEmpty()) {
            return Page.empty(pageable);
        }

        // 获取所有课程ID
        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        // 课程ID与课程映射
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        // 构建课程统计列表
        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> allCourseStats = new ArrayList<>();

        for (Long courseId : courseIds) {
            Course course = courseMap.get(courseId);

            // 查询课程学习时长
            Long totalDuration = learningRecordRepository.findTotalLearningDurationByCourse(courseId);

            // 查询课程学习活动次数
            Integer activityCount = 0;
            // 使用findByCourseIdAndTimeRange方法代替不存在的findByCourse_Id方法
            // 使用一个足够大的时间范围来获取所有记录
            LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.now().plusYears(1);
            List<LearningRecord> records = learningRecordRepository.findByCourseIdAndTimeRange(courseId, startTime, endTime);
            if (!records.isEmpty()) {
                activityCount = records.size();
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

            InstitutionLearningStatisticsVO.CourseStatisticsVO stat =
                    InstitutionLearningStatisticsVO.CourseStatisticsVO.builder()
                    .courseId(courseId)
                    .courseTitle(course.getTitle())
                    .totalDuration(totalDuration != null ? totalDuration : 0L)
                    .activityCount(activityCount)
                    .learnerCount(learnerCount)
                    .completionCount(completionCount != null ? completionCount : 0L)
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

        // 使用 new ArrayList<> 创建一个新的列表，而不是使用 subList
        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> pagedResults =
                new ArrayList<>(allCourseStats.subList(start, end));

        return new PageImpl<>(pagedResults, pageable, allCourseStats.size());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'institution_course_stats_' + #institutionId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> getInstitutionCourseStatistics(Long institutionId, Pageable pageable) {
        log.info("获取机构课程学习统计, 机构ID: {}, 页码: {}, 每页数量: {}",
                institutionId, pageable.getPageNumber(), pageable.getPageSize());

        // 获取机构课程
        // 使用查询所有课程然后过滤的方式代替不存在的findByInstitutionId方法
        List<Course> courses = courseRepository.findAll().stream()
                .filter(course -> course.getInstitutionId() != null && course.getInstitutionId().equals(institutionId))
                .collect(Collectors.toList());

        if (courses.isEmpty()) {
            return Page.empty(pageable);
        }

        // 直接使用课程列表，不需要创建映射

        // 构建课程统计列表
        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> allCourseStats = new ArrayList<>();

        for (Course course : courses) {
            Long courseId = course.getId();

            // 查询课程学习时长
            Long totalDuration = learningRecordRepository.findTotalLearningDurationByCourse(courseId);

            // 查询课程学习活动次数
            Integer activityCount = 0;
            // 使用findByCourseIdAndTimeRange方法代替不存在的findByCourse_Id方法
            LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.now().plusYears(1);
            List<LearningRecord> records = learningRecordRepository.findByCourseIdAndTimeRange(courseId, startTime, endTime);
            if (!records.isEmpty()) {
                activityCount = records.size();
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

            InstitutionLearningStatisticsVO.CourseStatisticsVO stat =
                    InstitutionLearningStatisticsVO.CourseStatisticsVO.builder()
                    .courseId(courseId)
                    .courseTitle(course.getTitle())
                    .totalDuration(totalDuration != null ? totalDuration : 0L)
                    .activityCount(activityCount)
                    .learnerCount(learnerCount)
                    .completionCount(completionCount != null ? completionCount : 0L)
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

        // 使用 new ArrayList<> 创建一个新的列表，而不是使用 subList
        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> pagedResults =
                new ArrayList<>(allCourseStats.subList(start, end));

        return new PageImpl<>(pagedResults, pageable, allCourseStats.size());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'course_statistics_' + #courseId")
    public InstitutionLearningStatisticsVO.CourseStatisticsVO getCourseLearningStatistics(Long courseId) {
        log.info("获取课程学习统计, 课程ID: {}", courseId);

        // 验证课程存在
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 查询课程学习时长
        Long totalDuration = learningRecordRepository.findTotalLearningDurationByCourse(courseId);

        // 查询课程学习活动次数
        Integer activityCount = 0;
        // 使用findByCourseIdAndTimeRange方法代替不存在的findByCourse_Id方法
        LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.now().plusYears(1);
        List<LearningRecord> records = learningRecordRepository.findByCourseIdAndTimeRange(courseId, startTime, endTime);
        if (!records.isEmpty()) {
            activityCount = records.size();
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
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'course_daily_stats_' + #courseId + '_' + #startDate + '_' + #endDate")
    public List<DailyLearningStatVO> getCourseDailyLearningStats(Long courseId, LocalDate startDate, LocalDate endDate) {
        log.info("获取课程每日学习统计, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                courseId, startDate, endDate);

        // 验证课程存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

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
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'course_activity_type_stats_' + #courseId")
    public List<ActivityTypeStatVO> getCourseActivityTypeStats(Long courseId) {
        log.info("获取课程活动类型统计, 课程ID: {}", courseId);

        // 验证课程存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

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
    // 禁用缓存以解决PageImpl序列化问题
    public Page<InstitutionLearningStatisticsVO.StudentLearningVO> getCourseStudentStatistics(Long courseId, Pageable pageable) {
        log.info("获取课程学生学习统计, 课程ID: {}, 页码: {}, 每页数量: {}",
                courseId, pageable.getPageNumber(), pageable.getPageSize());

        // 验证课程存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 获取课程学生学习统计
        List<Object[]> studentStats = learningRecordRepository.findStudentLearningStatsByCourse(courseId, pageable);

        List<InstitutionLearningStatisticsVO.StudentLearningVO> studentLearningList = new ArrayList<>();
        for (Object[] stat : studentStats) {
            Long userId = ((Number) stat[0]).longValue();
            String username = (String) stat[1];
            Long duration = stat[2] != null ? ((Number) stat[2]).longValue() : 0L;
            Integer count = stat[3] != null ? ((Number) stat[3]).intValue() : 0;
            LocalDateTime lastLearnTime = (LocalDateTime) stat[4];

            // 获取用户课程学习进度
            Integer progress = userCourseRepository.findProgressByUserIdAndCourseId(userId, courseId);

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
        return new PageImpl<>(studentLearningList, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'course_heatmap_' + #courseId + '_' + #startDate + '_' + #endDate")
    public LearningHeatmapVO getCourseLearningHeatmap(Long courseId, LocalDate startDate, LocalDate endDate) {
        log.info("获取课程学习热力图数据, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                courseId, startDate, endDate);

        // 验证课程存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询热力图数据
        List<Object[]> results = learningRecordRepository.findLearningHeatmapDataByCourse(
                courseId, startDateTime, endDateTime);

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
                .courseId(courseId)
                .heatmapData(heatmapData)
                .maxActivityCount(maxCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'course_progress_trend_' + #courseId + '_' + #startDate + '_' + #endDate")
    public LearningProgressTrendVO getCourseLearningProgressTrend(Long courseId, LocalDate startDate, LocalDate endDate) {
        log.info("获取课程学习进度趋势, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                courseId, startDate, endDate);

        // 验证课程存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

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
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'total_learning_duration'")
    public Long getTotalLearningDuration() {
        log.info("获取平台总学习时长");

        // 查询所有学习记录的总时长
        Long totalDuration = learningRecordRepository.findAll().stream()
                .filter(lr -> lr.getDurationSeconds() != null)
                .mapToLong(LearningRecord::getDurationSeconds)
                .sum();

        return totalDuration;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'today_learning_duration'")
    public Long getTodayLearningDuration() {
        log.info("获取平台今日学习时长");

        // 获取今天的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 查询今日学习记录的总时长
        Long todayDuration = learningRecordRepository.findAll().stream()
                .filter(lr -> lr.getActivityStartTime() != null &&
                        lr.getActivityStartTime().isAfter(startOfDay) &&
                        lr.getActivityStartTime().isBefore(endOfDay) &&
                        lr.getDurationSeconds() != null)
                .mapToLong(LearningRecord::getDurationSeconds)
                .sum();

        return todayDuration;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'total_learner_count'")
    public Long getTotalLearnerCount() {
        log.info("获取平台学习人数");

        // 查询所有有学习记录的用户数量
        Long learnerCount = learningRecordRepository.findAll().stream()
                .map(LearningRecord::getUserId)
                .distinct()
                .count();

        return learnerCount;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'all_activity_type_stats'")
    public List<ActivityTypeStatVO> getAllActivityTypeStats() {
        log.info("获取平台活动类型统计");

        // 按活动类型分组统计学习时长和活动次数
        Map<String, List<LearningRecord>> recordsByType = learningRecordRepository.findAll().stream()
                .filter(lr -> lr.getActivityType() != null && lr.getDurationSeconds() != null)
                .collect(Collectors.groupingBy(LearningRecord::getActivityType));

        List<ActivityTypeStatVO> stats = new ArrayList<>();

        for (Map.Entry<String, List<LearningRecord>> entry : recordsByType.entrySet()) {
            String activityType = entry.getKey();
            List<LearningRecord> records = entry.getValue();

            Long totalDuration = records.stream()
                    .mapToLong(LearningRecord::getDurationSeconds)
                    .sum();

            Integer count = records.size();

            LearningActivityType type = LearningActivityType.getByCode(activityType);
            String description = type != null ? type.getDescription() : activityType;

            stats.add(ActivityTypeStatVO.builder()
                    .activityType(activityType)
                    .activityTypeDescription(description)
                    .totalDurationSeconds(totalDuration)
                    .activityCount(count)
                    .build());
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'all_daily_stats_' + #startDate + '_' + #endDate")
    public List<DailyLearningStatVO> getAllDailyLearningStats(LocalDate startDate, LocalDate endDate) {
        log.info("获取平台每日学习统计, 开始日期: {}, 结束日期: {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 获取指定时间范围内的所有学习记录
        List<LearningRecord> records = learningRecordRepository.findAll().stream()
                .filter(lr -> lr.getActivityStartTime() != null &&
                        lr.getActivityStartTime().isAfter(startDateTime) &&
                        lr.getActivityStartTime().isBefore(endDateTime) &&
                        lr.getDurationSeconds() != null)
                .collect(Collectors.toList());

        // 按日期分组
        Map<String, List<LearningRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(lr ->
                        lr.getActivityStartTime().toLocalDate().toString()));

        List<DailyLearningStatVO> stats = new ArrayList<>();

        for (Map.Entry<String, List<LearningRecord>> entry : recordsByDate.entrySet()) {
            String date = entry.getKey();
            List<LearningRecord> dailyRecords = entry.getValue();

            Long duration = dailyRecords.stream()
                    .mapToLong(LearningRecord::getDurationSeconds)
                    .sum();

            Integer count = dailyRecords.size();

            stats.add(DailyLearningStatVO.builder()
                    .date(date)
                    .durationSeconds(duration)
                    .activityCount(count)
                    .build());
        }

        // 按日期排序
        stats.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    // 暂时禁用缓存，以解决 ArrayList$SubList 序列化问题
    // @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'institution_ranking_' + #sortBy + '_' + #limit")
    public List<InstitutionLearningStatisticsVO.InstitutionStatisticsVO> getInstitutionRanking(String sortBy, Integer limit) {
        log.info("获取机构学习统计排行, 排序字段: {}, 数量限制: {}", sortBy, limit);

        // 获取所有机构
        List<Object[]> institutionData = learningRecordRepository.findInstitutionStatistics();

        List<InstitutionLearningStatisticsVO.InstitutionStatisticsVO> institutionStats = new ArrayList<>();

        for (Object[] data : institutionData) {
            Long institutionId = ((Number) data[0]).longValue();
            String institutionName = (String) data[1];
            String logo = (String) data[2];
            Long studentCount = ((Number) data[3]).longValue();
            Integer courseCount = ((Number) data[4]).intValue();
            Long totalDuration = data[5] != null ? ((Number) data[5]).longValue() : 0L;
            Integer activityCount = data[6] != null ? ((Number) data[6]).intValue() : 0;

            institutionStats.add(InstitutionLearningStatisticsVO.InstitutionStatisticsVO.builder()
                    .institutionId(institutionId)
                    .institutionName(institutionName)
                    .logo(logo)
                    .studentCount(studentCount)
                    .courseCount(courseCount)
                    .totalDuration(totalDuration)
                    .activityCount(activityCount)
                    .build());
        }

        // 根据排序字段排序
        switch (sortBy) {
            case "studentCount":
                institutionStats.sort((a, b) -> b.getStudentCount().compareTo(a.getStudentCount()));
                break;
            case "courseCount":
                institutionStats.sort((a, b) -> b.getCourseCount().compareTo(a.getCourseCount()));
                break;
            case "activityCount":
                institutionStats.sort((a, b) -> b.getActivityCount().compareTo(a.getActivityCount()));
                break;
            case "totalDuration":
            default:
                institutionStats.sort((a, b) -> b.getTotalDuration().compareTo(a.getTotalDuration()));
                break;
        }

        // 限制数量
        if (institutionStats.size() > limit) {
            // 使用 new ArrayList<> 创建一个新的列表，而不是使用 subList
            // 这样可以避免 ArrayList$SubList 序列化问题
            return new ArrayList<>(institutionStats.subList(0, limit));
        }

        return institutionStats;
    }

    @Override
    @Transactional(readOnly = true)
    // 暂时禁用缓存，以解决 ArrayList$SubList 序列化问题
    // @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'course_ranking_' + #sortBy + '_' + #institutionId + '_' + #limit")
    public List<InstitutionLearningStatisticsVO.CourseStatisticsVO> getCourseRanking(String sortBy, Long institutionId, Integer limit) {
        log.info("获取课程学习统计排行, 排序字段: {}, 机构ID: {}, 数量限制: {}", sortBy, institutionId, limit);

        // 获取课程列表 - 只获取已发布的课程
        List<Course> courses;
        if (institutionId != null) {
            // 获取指定机构的已发布课程
            Pageable unpaged = Pageable.unpaged(); // 不分页，获取所有结果
            Page<Course> coursePage = courseRepository.findByInstitutionIdAndStatusAndIsPublishedVersion(
                    institutionId,
                    CourseStatus.PUBLISHED.getValue(),
                    true,
                    unpaged);
            courses = coursePage.getContent();
        } else {
            // 获取所有已发布课程
            Pageable unpaged = Pageable.unpaged(); // 不分页，获取所有结果
            Page<Course> coursePage = courseRepository.findByStatusAndIsPublishedVersion(
                    CourseStatus.PUBLISHED.getValue(),
                    true,
                    unpaged);
            courses = coursePage.getContent();
        }

        if (courses.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建课程统计列表
        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> courseStats = new ArrayList<>();

        for (Course course : courses) {
            Long courseId = course.getId();

            // 查询课程学习时长
            Long totalDuration = learningRecordRepository.findTotalLearningDurationByCourse(courseId);

            // 查询课程学习活动次数
            Integer activityCount = 0;
            LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.now().plusYears(1);
            List<LearningRecord> records = learningRecordRepository.findByCourseIdAndTimeRange(courseId, startTime, endTime);
            if (!records.isEmpty()) {
                activityCount = records.size();
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

            courseStats.add(InstitutionLearningStatisticsVO.CourseStatisticsVO.builder()
                    .courseId(courseId)
                    .courseTitle(course.getTitle())
                    .totalDuration(totalDuration != null ? totalDuration : 0L)
                    .activityCount(activityCount)
                    .learnerCount(learnerCount)
                    .completionCount(completionCount != null ? completionCount : 0L)
                    .averageProgress(averageProgress)
                    .build());
        }

        // 根据排序字段排序
        switch (sortBy) {
            case "learnerCount":
                courseStats.sort((a, b) -> b.getLearnerCount().compareTo(a.getLearnerCount()));
                break;
            case "activityCount":
                courseStats.sort((a, b) -> b.getActivityCount().compareTo(a.getActivityCount()));
                break;
            case "favoriteCount":
                // 注意：CourseStatisticsVO中没有favoriteCount字段，这里使用完成人数代替
                courseStats.sort((a, b) -> b.getCompletionCount().compareTo(a.getCompletionCount()));
                break;
            case "totalDuration":
            default:
                courseStats.sort((a, b) -> b.getTotalDuration().compareTo(a.getTotalDuration()));
                break;
        }

        // 限制数量
        if (courseStats.size() > limit) {
            // 使用 new ArrayList<> 创建一个新的列表，而不是使用 subList
            // 这样可以避免 ArrayList$SubList 序列化问题
            return new ArrayList<>(courseStats.subList(0, limit));
        }

        return courseStats;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'institution_course_distribution'")
    public List<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO> getInstitutionCourseDistribution() {
        log.info("获取机构课程占比统计");

        // 获取所有机构及其课程数量
        List<Object[]> institutionCourseData = learningRecordRepository.findInstitutionCourseDistribution();

        // 计算总课程数
        int totalCourses = institutionCourseData.stream()
                .mapToInt(data -> ((Number) data[3]).intValue())
                .sum();

        List<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO> distribution = new ArrayList<>();

        for (Object[] data : institutionCourseData) {
            Long institutionId = ((Number) data[0]).longValue();
            String institutionName = (String) data[1];
            String logo = (String) data[2];
            Integer courseCount = ((Number) data[3]).intValue();

            // 计算占比
            double percentage = totalCourses > 0 ? (double) courseCount / totalCourses : 0.0;

            distribution.add(InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO.builder()
                    .institutionId(institutionId)
                    .institutionName(institutionName)
                    .logo(logo)
                    .courseCount(courseCount)
                    .percentage(percentage)
                    .build());
        }

        // 按课程数量降序排序
        distribution.sort((a, b) -> b.getCourseCount().compareTo(a.getCourseCount()));

        return distribution;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'user_course_heatmap_' + #courseId + '_' + #userId + '_' + #startDate + '_' + #endDate")
    public LearningHeatmapVO getUserCourseLearningHeatmap(Long courseId, Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户课程学习热力图数据, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                courseId, userId, startDate, endDate);

        // 验证课程存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询用户课程热力图数据
        List<Object[]> results = learningRecordRepository.findLearningHeatmapDataByUserAndCourse(
                userId, courseId, startDateTime, endDateTime);

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
                .courseId(courseId)
                .heatmapData(heatmapData)
                .maxActivityCount(maxCount)
                .build();
    }

    /**
     * 获取机构每日学习统计
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'institution_daily_stats_' + #institutionId + '_' + #startDate + '_' + #endDate")
    public List<DailyLearningStatVO> getInstitutionDailyLearningStats(Long institutionId, LocalDate startDate, LocalDate endDate) {
        log.info("获取机构每日学习统计, 机构ID: {}, 开始日期: {}, 结束日期: {}",
                institutionId, startDate, endDate);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

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

    /**
     * 获取机构活动类型统计
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'institution_activity_type_stats_' + #institutionId")
    public List<ActivityTypeStatVO> getInstitutionActivityTypeStats(Long institutionId) {
        log.info("获取机构活动类型统计, 机构ID: {}", institutionId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        List<Object[]> results = learningRecordRepository.findLearningStatsByActivityTypeForInstitution(institutionId);

        // 计算总学习时长（用于计算百分比）
        Number totalDurationNum = learningRecordRepository.findTotalLearningDurationByInstitution(institutionId);
        Long totalDuration = totalDurationNum != null ? totalDurationNum.longValue() : 0L;

        List<ActivityTypeStatVO> stats = new ArrayList<>();
        for (Object[] result : results) {
            String activityType = (String) result[0];
            Long duration = result[1] != null ? ((Number) result[1]).longValue() : 0L;
            Integer count = result[2] != null ? ((Number) result[2]).intValue() : 0;

            // 计算百分比
            Double percentage = totalDuration > 0 ? (double) duration / totalDuration : 0.0;

            LearningActivityType type = LearningActivityType.getByCode(activityType);
            String description = type != null ? type.getDescription() : activityType;

            stats.add(ActivityTypeStatVO.builder()
                    .activityType(activityType)
                    .activityTypeDescription(description)
                    .totalDurationSeconds(duration)
                    .activityCount(count)
                    .percentage(percentage)
                    .build());
        }

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'user_course_progress_trend_' + #courseId + '_' + #userId + '_' + #startDate + '_' + #endDate")
    public LearningProgressTrendVO getUserCourseLearningProgressTrend(Long courseId, Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户课程学习进度趋势, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                courseId, userId, startDate, endDate);

        // 验证课程存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 查询进度趋势数据
        List<Object[]> results = userCourseRepository.findDailyProgressTrendByUserAndCourse(
                userId, courseId, startDateTime, endDateTime);

        // 处理查询结果
        List<LearningProgressTrendVO.DailyProgressVO> progressData = new ArrayList<>();

        for (Object[] result : results) {
            String date = (String) result[0];
            Double progress = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
            Integer activityCount = result[2] != null ? ((Number) result[2]).intValue() : 0;

            progressData.add(LearningProgressTrendVO.DailyProgressVO.builder()
                    .date(date)
                    .averageProgress(progress)
                    .activeUserCount(activityCount)
                    .build());
        }

        // 按日期排序
        progressData.sort(Comparator.comparing(LearningProgressTrendVO.DailyProgressVO::getDate));

        return LearningProgressTrendVO.builder()
                .courseId(courseId)
                .progressData(progressData)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.ADMIN_STATS_CACHE, key = "'institution_statistics_' + #institutionId")
    public InstitutionLearningStatisticsVO getInstitutionLearningStatistics(Long institutionId) {
        log.info("管理员获取机构学习统计, 机构ID: {}", institutionId);

        // 验证机构存在
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));

        // 直接调用机构学习统计服务
        InstitutionLearningStatisticsVO statisticsVO = institutionLearningStatisticsService.getInstitutionLearningStatistics(institutionId);

        log.info("管理员成功获取机构学习统计, 机构ID: {}, 学习人数: {}, 学习时长: {}秒",
                institutionId, statisticsVO.getTotalLearners(), statisticsVO.getTotalLearningDuration());

        return statisticsVO;
    }

    @Override
    @CacheEvict(value = CacheConfig.ADMIN_STATS_CACHE, allEntries = true)
    public void clearStatisticsCache() {
        log.info("清除所有统计缓存");
    }
}
