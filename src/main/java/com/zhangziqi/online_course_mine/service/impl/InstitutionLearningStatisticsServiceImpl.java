package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionLearningStatisticsVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.LearningRecordRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.service.InstitutionLearningStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Long getInstitutionTodayLearningDuration(Long institutionId) {
        log.info("获取机构今日学习时长, 机构ID: {}", institutionId);
        
        Long duration = learningRecordRepository.findTodayLearningDurationByInstitution(institutionId);
        return duration != null ? duration : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getInstitutionTotalLearningDuration(Long institutionId) {
        log.info("获取机构总学习时长, 机构ID: {}", institutionId);
        
        Long duration = learningRecordRepository.findTotalLearningDurationByInstitution(institutionId);
        return duration != null ? duration : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getInstitutionLearnerCount(Long institutionId) {
        log.info("获取机构学习人数, 机构ID: {}", institutionId);
        
        Long count = learningRecordRepository.countUniqueUsersByInstitution(institutionId);
        return count != null ? count : 0L;
    }
} 