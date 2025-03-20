package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.LearningStatisticsVO;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.repository.UserWrongQuestionRepository;
import com.zhangziqi.online_course_mine.service.LearningStatisticsService;
import com.zhangziqi.online_course_mine.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学习统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStatisticsServiceImpl implements LearningStatisticsService {
    
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserWrongQuestionRepository wrongQuestionRepository;
    private final WrongQuestionService wrongQuestionService;
    
    @Value("${learning.statistics.days:30}")
    private int statisticsDays;
    
    @Override
    @Transactional(readOnly = true)
    public LearningStatisticsVO getUserLearningStatistics(Long userId) {
        log.info("获取用户学习统计数据, 用户ID: {}", userId);
        
        // 验证用户存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        
        // 获取用户的所有课程学习记录
        List<UserCourse> userCourses = userCourseRepository.findByUser_IdAndStatus(
                userId, UserCourseStatus.NORMAL.getValue());
        
        // 基本统计数据
        int totalCourses = userCourses.size();
        int completedCourses = (int) userCourses.stream()
                .filter(uc -> uc.getProgress() != null && uc.getProgress() >= 100)
                .count();
        
        long totalLearningDuration = userCourses.stream()
                .mapToLong(UserCourse::getLearnDuration)
                .sum();
        
        // 错题统计
        long wrongQuestions = wrongQuestionService.countUserWrongQuestions(userId);
        long unresolvedWrongQuestions = wrongQuestionService.countUserUnresolvedWrongQuestions(userId);
        
        // 获取每日学习时长统计
        List<LearningStatisticsVO.DailyLearningVO> dailyLearning = getDailyLearningStatistics(userId);
        
        // 获取课程学习统计
        List<LearningStatisticsVO.CourseStatisticsVO> courseStatistics = userCourses.stream()
                .map(this::mapToCourseStatistics)
                .collect(Collectors.toList());
        
        // 构建统计VO
        LearningStatisticsVO statistics = LearningStatisticsVO.builder()
                .userId(userId)
                .totalCourses(totalCourses)
                .completedCourses(completedCourses)
                .totalLearningDuration(totalLearningDuration)
                .todayLearningDuration(calculateTodayLearningDuration(dailyLearning))
                .weekLearningDuration(calculateWeekLearningDuration(dailyLearning))
                .monthLearningDuration(calculateMonthLearningDuration(dailyLearning))
                .learningDays(calculateLearningDays(dailyLearning))
                .maxConsecutiveDays(calculateMaxConsecutiveDays(dailyLearning))
                .currentConsecutiveDays(calculateCurrentConsecutiveDays(dailyLearning))
                .totalQuestions(0) // 暂不统计
                .correctQuestions(0) // 暂不统计
                .wrongQuestions((int) wrongQuestions)
                .dailyLearning(dailyLearning)
                .courseStatistics(courseStatistics)
                .build();
        
        log.info("成功获取用户学习统计数据");
        return statistics;
    }
    
    @Override
    @Transactional(readOnly = true)
    public LearningStatisticsVO.CourseStatisticsVO getUserCourseLearningStatistics(Long userId, Long courseId) {
        log.info("获取用户课程学习统计数据, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        UserCourse userCourse = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到学习记录"));
        
        return mapToCourseStatistics(userCourse);
    }
    
    @Override
    @Transactional
    public void resetUserCourseProgress(Long userId, Long courseId) {
        log.info("重置用户课程学习进度, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        UserCourse userCourse = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到学习记录"));
        
        // 只能重置自己的课程进度
        if (!userCourse.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作此学习记录");
        }
        
        // 重置进度
        userCourse.setProgress(0);
        userCourse.setCurrentChapterId(null);
        userCourse.setCurrentSectionId(null);
        userCourse.setCurrentSectionProgress(0);
        
        // 保留累计学习时长和最后学习时间
        userCourseRepository.save(userCourse);
        
        log.info("成功重置用户课程学习进度");
    }
    
    /**
     * 获取每日学习时长统计
     */
    private List<LearningStatisticsVO.DailyLearningVO> getDailyLearningStatistics(Long userId) {
        // 这里应该是从学习记录表中统计每日学习时长
        // 由于缺少每日学习时长记录表，这里暂时返回空列表
        
        List<LearningStatisticsVO.DailyLearningVO> result = new ArrayList<>();
        
        // 获取过去30天的日期
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 0; i < statisticsDays; i++) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            
            // 这里应该查询该日期的学习时长
            long duration = 0; // 暂时返回0
            
            LearningStatisticsVO.DailyLearningVO dailyLearning = LearningStatisticsVO.DailyLearningVO.builder()
                    .date(dateStr)
                    .duration(duration)
                    .build();
            
            result.add(dailyLearning);
        }
        
        return result;
    }
    
    /**
     * 将UserCourse映射为CourseStatisticsVO
     */
    private LearningStatisticsVO.CourseStatisticsVO mapToCourseStatistics(UserCourse userCourse) {
        LocalDateTime lastLearnTime = userCourse.getLastLearnAt();
        long lastLearnTimeMillis = lastLearnTime != null ? 
                lastLearnTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : 0;
        
        return LearningStatisticsVO.CourseStatisticsVO.builder()
                .courseId(userCourse.getCourse().getId())
                .courseTitle(userCourse.getCourse().getTitle())
                .courseCover(userCourse.getCourse().getCoverImage())
                .progress(userCourse.getProgress())
                .learningDuration((long) userCourse.getLearnDuration())
                .lastLearnTime(lastLearnTimeMillis)
                .build();
    }
    
    /**
     * 计算今日学习时长
     */
    private Long calculateTodayLearningDuration(List<LearningStatisticsVO.DailyLearningVO> dailyLearning) {
        if (dailyLearning == null || dailyLearning.isEmpty()) {
            return 0L;
        }
        
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        return dailyLearning.stream()
                .filter(d -> d.getDate().equals(todayStr))
                .mapToLong(LearningStatisticsVO.DailyLearningVO::getDuration)
                .sum();
    }
    
    /**
     * 计算本周学习时长
     */
    private Long calculateWeekLearningDuration(List<LearningStatisticsVO.DailyLearningVO> dailyLearning) {
        if (dailyLearning == null || dailyLearning.isEmpty()) {
            return 0L;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        
        return dailyLearning.stream()
                .filter(d -> {
                    LocalDate date = LocalDate.parse(d.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return !date.isBefore(weekStart);
                })
                .mapToLong(LearningStatisticsVO.DailyLearningVO::getDuration)
                .sum();
    }
    
    /**
     * 计算本月学习时长
     */
    private Long calculateMonthLearningDuration(List<LearningStatisticsVO.DailyLearningVO> dailyLearning) {
        if (dailyLearning == null || dailyLearning.isEmpty()) {
            return 0L;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        
        return dailyLearning.stream()
                .filter(d -> {
                    LocalDate date = LocalDate.parse(d.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return !date.isBefore(monthStart);
                })
                .mapToLong(LearningStatisticsVO.DailyLearningVO::getDuration)
                .sum();
    }
    
    /**
     * 计算学习天数
     */
    private Integer calculateLearningDays(List<LearningStatisticsVO.DailyLearningVO> dailyLearning) {
        if (dailyLearning == null || dailyLearning.isEmpty()) {
            return 0;
        }
        
        return (int) dailyLearning.stream()
                .filter(d -> d.getDuration() > 0)
                .count();
    }
    
    /**
     * 计算最长连续学习天数
     */
    private Integer calculateMaxConsecutiveDays(List<LearningStatisticsVO.DailyLearningVO> dailyLearning) {
        if (dailyLearning == null || dailyLearning.isEmpty()) {
            return 0;
        }
        
        // 按日期排序
        Map<LocalDate, Long> dateMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (LearningStatisticsVO.DailyLearningVO daily : dailyLearning) {
            LocalDate date = LocalDate.parse(daily.getDate(), formatter);
            dateMap.put(date, daily.getDuration());
        }
        
        int maxConsecutive = 0;
        int currentConsecutive = 0;
        
        LocalDate current = LocalDate.now();
        for (int i = 0; i < statisticsDays; i++) {
            Long duration = dateMap.getOrDefault(current, 0L);
            
            if (duration > 0) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 0;
            }
            
            current = current.minusDays(1);
        }
        
        return maxConsecutive;
    }
    
    /**
     * 计算当前连续学习天数
     */
    private Integer calculateCurrentConsecutiveDays(List<LearningStatisticsVO.DailyLearningVO> dailyLearning) {
        if (dailyLearning == null || dailyLearning.isEmpty()) {
            return 0;
        }
        
        // 按日期排序
        Map<LocalDate, Long> dateMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (LearningStatisticsVO.DailyLearningVO daily : dailyLearning) {
            LocalDate date = LocalDate.parse(daily.getDate(), formatter);
            dateMap.put(date, daily.getDuration());
        }
        
        int currentConsecutive = 0;
        
        LocalDate current = LocalDate.now();
        while (currentConsecutive < statisticsDays) {
            Long duration = dateMap.getOrDefault(current, 0L);
            
            if (duration > 0) {
                currentConsecutive++;
                current = current.minusDays(1);
            } else {
                break;
            }
        }
        
        return currentConsecutive;
    }
} 