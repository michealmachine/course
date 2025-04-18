package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.config.CacheConfig;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionLearningStatisticsVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.LearningRecordRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.service.impl.InstitutionLearningStatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstitutionLearningStatisticsServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LearningRecordRepository learningRecordRepository;

    @Mock
    private UserCourseRepository userCourseRepository;

    @InjectMocks
    private InstitutionLearningStatisticsServiceImpl statisticsService;

    private Institution institution;
    private Course course1;
    private Course course2;
    private User user1;
    private User user2;
    private LearningRecord record1;
    private LearningRecord record2;
    private UserCourse userCourse1;
    private UserCourse userCourse2;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        institution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .build();

        user1 = User.builder()
                .id(1L)
                .username("user1")
                .institution(institution)
                .build();

        user2 = User.builder()
                .id(2L)
                .username("user2")
                .institution(institution)
                .build();

        course1 = Course.builder()
                .id(1L)
                .title("测试课程1")
                .institution(institution)
                .build();

        course2 = Course.builder()
                .id(2L)
                .title("测试课程2")
                .institution(institution)
                .build();

        record1 = LearningRecord.builder()
                .id(1L)
                .user(user1)
                .course(course1)
                .activityType(LearningActivityType.VIDEO_WATCH.getCode())
                .activityStartTime(LocalDateTime.now().minusDays(1))
                .activityEndTime(LocalDateTime.now().minusDays(1).plusHours(1))
                .durationSeconds(3600)
                .build();

        record2 = LearningRecord.builder()
                .id(2L)
                .user(user2)
                .course(course2)
                .activityType(LearningActivityType.QUIZ_ATTEMPT.getCode())
                .activityStartTime(LocalDateTime.now().minusHours(2))
                .activityEndTime(LocalDateTime.now().minusHours(1))
                .durationSeconds(3600)
                .build();
        
        userCourse1 = UserCourse.builder()
                .user(user1)
                .course(course1)
                .status(UserCourseStatus.NORMAL.getValue()) // 正常状态
                .build();
        
        userCourse2 = UserCourse.builder()
                .user(user2)
                .course(course2)
                .status(UserCourseStatus.NORMAL.getValue()) // 正常状态
                .build();
    }

    @Test
    @DisplayName("获取机构学习统计概览")
    void testGetInstitutionLearningStatistics() {
        // 设置模拟行为
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(learningRecordRepository.countUniqueUsersByInstitution(1L)).thenReturn(2L);
        when(learningRecordRepository.findTotalLearningDurationByInstitution(1L)).thenReturn(7200L);
        when(learningRecordRepository.findTodayLearningDurationByInstitution(1L)).thenReturn(3600L);
        
        LocalDateTime weekStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekEnd = LocalDate.now().atTime(LocalTime.MAX);
        when(learningRecordRepository.findByInstitutionIdAndTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(record1, record2));
        
        when(learningRecordRepository.findLearningStatsByCourseForInstitution(1L))
                .thenReturn(Arrays.asList(
                        new Object[]{1L, "测试课程1", 3600L, 1},
                        new Object[]{2L, "测试课程2", 3600L, 1}
                ));
                
        // 为概览测试添加课程统计相关的mock
        when(courseRepository.findByInstitution(any(Institution.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(course1, course2)));
        
        Object[] learnerCount1 = new Object[]{20L};
        when(userCourseRepository.countLearnersByCourseId(1L))
                .thenReturn(Collections.singletonList(learnerCount1));
        
        Object[] learnerCount2 = new Object[]{15L};
        when(userCourseRepository.countLearnersByCourseId(2L))
                .thenReturn(Collections.singletonList(learnerCount2));
        
        when(userCourseRepository.countByProgress(1L, 100))
                .thenReturn(10L);
        when(userCourseRepository.countByProgress(2L, 100))
                .thenReturn(5L);
        
        when(userCourseRepository.getAverageProgressByCourseId(1L))
                .thenReturn(75.0);
        when(userCourseRepository.getAverageProgressByCourseId(2L))
                .thenReturn(60.0);
                
        // 为getMostActiveUsers添加mock
        when(learningRecordRepository.findMostActiveUsersByInstitution(eq(1L), any(Pageable.class)))
                .thenReturn(Arrays.asList(
                        new Object[]{1L, "user1", 5400L, 15},
                        new Object[]{2L, "user2", 3600L, 10}
                ));
                
        // 为getDailyLearningStats添加mock
        when(learningRecordRepository.findDailyLearningStatsByInstitutionId(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(
                        new Object[]{"2023-06-01", 3600L, 2},
                        new Object[]{"2023-06-02", 7200L, 3}
                ));
                
        // 为getActivityTypeStats添加mock
        when(learningRecordRepository.findLearningStatsByActivityTypeForInstitution(1L))
                .thenReturn(Arrays.asList(
                        new Object[]{LearningActivityType.VIDEO_WATCH.getCode(), 3600L, 2},
                        new Object[]{LearningActivityType.QUIZ_ATTEMPT.getCode(), 1800L, 5}
                ));
        
        // 执行方法
        InstitutionLearningStatisticsVO result = statisticsService.getInstitutionLearningStatistics(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getInstitutionId());
        assertEquals("测试机构", result.getInstitutionName());
        assertEquals(2L, result.getTotalLearners());
        assertEquals(7200L, result.getTotalLearningDuration());
        assertEquals(3600L, result.getTodayLearningDuration());
        assertEquals(2, result.getTotalActiveCourses());
    }
    
    @Test
    @DisplayName("获取机构学习统计概览 - 测试缓存")
    void testGetInstitutionLearningStatisticsCache() {
        // 设置模拟行为
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution));
        when(learningRecordRepository.countUniqueUsersByInstitution(1L)).thenReturn(2L);
        when(learningRecordRepository.findTotalLearningDurationByInstitution(1L)).thenReturn(7200L);
        when(learningRecordRepository.findTodayLearningDurationByInstitution(1L)).thenReturn(3600L);
        
        when(learningRecordRepository.findByInstitutionIdAndTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(record1, record2));
        
        when(learningRecordRepository.findLearningStatsByCourseForInstitution(1L))
                .thenReturn(Arrays.asList(
                        new Object[]{1L, "测试课程1", 3600L, 1},
                        new Object[]{2L, "测试课程2", 3600L, 1}
                ));
                
        when(courseRepository.findByInstitution(any(Institution.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(course1, course2)));
        
        Object[] learnerCount1 = new Object[]{20L};
        when(userCourseRepository.countLearnersByCourseId(1L))
                .thenReturn(Collections.singletonList(learnerCount1));
        
        Object[] learnerCount2 = new Object[]{15L};
        when(userCourseRepository.countLearnersByCourseId(2L))
                .thenReturn(Collections.singletonList(learnerCount2));
        
        when(userCourseRepository.countByProgress(anyLong(), anyInt())).thenReturn(10L);
        when(userCourseRepository.getAverageProgressByCourseId(anyLong())).thenReturn(75.0);
        
        // 由于不再使用缓存模拟，只能通过验证方法调用次数来测试缓存逻辑
        // 注意：这个测试无法完全模拟缓存行为，但可以验证基本功能
        
        // 第一次调用
        InstitutionLearningStatisticsVO result1 = statisticsService.getInstitutionLearningStatistics(1L);
        
        // 第二次调用
        InstitutionLearningStatisticsVO result2 = statisticsService.getInstitutionLearningStatistics(1L);
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        
        // 由于缓存注解在测试环境不会生效，所以方法会被调用两次
        // 这里验证方法被调用的次数
        verify(institutionRepository, times(2)).findById(1L);
    }

    @Test
    @DisplayName("获取机构每日学习统计")
    void testGetInstitutionDailyLearningStats() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        // 模拟查询结果
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"2023-06-01", 3600L, 2},
                new Object[]{"2023-06-02", 7200L, 3}
        );
        
        when(learningRecordRepository.findDailyLearningStatsByInstitutionId(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResults);
        
        // 执行方法
        List<DailyLearningStatVO> result = statisticsService.getInstitutionDailyLearningStats(1L, startDate, endDate);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("2023-06-01", result.get(0).getDate());
        assertEquals(3600L, result.get(0).getDurationSeconds());
        assertEquals(2, result.get(0).getActivityCount());
    }
    
    @Test
    @DisplayName("获取机构每日学习统计 - 测试方法逻辑")
    void testGetInstitutionDailyLearningStatsLogic() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        // 模拟查询结果
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"2023-06-01", 3600L, 2},
                new Object[]{"2023-06-02", 7200L, 3}
        );
        
        when(learningRecordRepository.findDailyLearningStatsByInstitutionId(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResults);
        
        // 执行方法两次
        List<DailyLearningStatVO> result1 = statisticsService.getInstitutionDailyLearningStats(1L, startDate, endDate);
        List<DailyLearningStatVO> result2 = statisticsService.getInstitutionDailyLearningStats(1L, startDate, endDate);
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        
        // 验证方法调用 - 由于缓存在测试环境不会生效，所以会调用两次
        verify(learningRecordRepository, times(2)).findDailyLearningStatsByInstitutionId(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("获取机构活动类型统计")
    void testGetInstitutionActivityTypeStats() {
        // 模拟查询结果
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{LearningActivityType.VIDEO_WATCH.getCode(), 3600L, 2},
                new Object[]{LearningActivityType.QUIZ_ATTEMPT.getCode(), 1800L, 5}
        );
        
        when(learningRecordRepository.findLearningStatsByActivityTypeForInstitution(1L))
                .thenReturn(mockResults);
        
        // 执行方法
        List<ActivityTypeStatVO> result = statisticsService.getInstitutionActivityTypeStats(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LearningActivityType.VIDEO_WATCH.getCode(), result.get(0).getActivityType());
        assertEquals(LearningActivityType.VIDEO_WATCH.getDescription(), result.get(0).getActivityTypeDescription());
        assertEquals(3600L, result.get(0).getTotalDurationSeconds());
        assertEquals(2, result.get(0).getActivityCount());
    }
    
    @Test
    @DisplayName("获取机构活动类型统计 - 测试方法逻辑")
    void testGetInstitutionActivityTypeStatsLogic() {
        // 模拟查询结果
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{LearningActivityType.VIDEO_WATCH.getCode(), 3600L, 2},
                new Object[]{LearningActivityType.QUIZ_ATTEMPT.getCode(), 1800L, 5}
        );
        
        when(learningRecordRepository.findLearningStatsByActivityTypeForInstitution(1L))
                .thenReturn(mockResults);
        
        // 执行方法两次
        List<ActivityTypeStatVO> result1 = statisticsService.getInstitutionActivityTypeStats(1L);
        List<ActivityTypeStatVO> result2 = statisticsService.getInstitutionActivityTypeStats(1L);
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        
        // 验证方法调用 - 由于缓存在测试环境不会生效，所以会调用两次
        verify(learningRecordRepository, times(2)).findLearningStatsByActivityTypeForInstitution(1L);
    }

    @Test
    @DisplayName("获取机构课程学习统计")
    void testGetInstitutionCourseStatistics() {
        // 模拟查询结果 - 确保返回非null的Page对象
        List<Course> courses = Arrays.asList(course1, course2);
        Page<Course> coursePage = new PageImpl<>(courses);
        when(courseRepository.findByInstitution(any(Institution.class), any(Pageable.class)))
                .thenReturn(coursePage);
        
        when(learningRecordRepository.findLearningStatsByCourseForInstitution(1L))
                .thenReturn(Arrays.asList(
                        new Object[]{1L, "测试课程1", 3600L, 10},
                        new Object[]{2L, "测试课程2", 1800L, 5}
                ));
        
        // 设置正确的返回结构：第0个元素是数字类型
        Object[] learnerCount1 = new Object[]{20L};
        Object[] learnerCount2 = new Object[]{15L};
        
        when(userCourseRepository.countLearnersByCourseId(1L))
                .thenReturn(Collections.singletonList(learnerCount1));
        
        when(userCourseRepository.countLearnersByCourseId(2L))
                .thenReturn(Collections.singletonList(learnerCount2));
        
        when(userCourseRepository.countByProgress(1L, 100))
                .thenReturn(10L);
        
        when(userCourseRepository.countByProgress(2L, 100))
                .thenReturn(5L);
        
        when(userCourseRepository.getAverageProgressByCourseId(1L))
                .thenReturn(75.0);
        
        when(userCourseRepository.getAverageProgressByCourseId(2L))
                .thenReturn(60.0);
        
        // 执行方法
        Pageable pageable = PageRequest.of(0, 10);
        Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> result = 
                statisticsService.getInstitutionCourseStatistics(1L, pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getCourseId());
        assertEquals("测试课程1", result.getContent().get(0).getCourseTitle());
        assertEquals(3600L, result.getContent().get(0).getTotalDuration());
        assertEquals(10, result.getContent().get(0).getActivityCount());
        assertEquals(20L, result.getContent().get(0).getLearnerCount());
        assertEquals(10L, result.getContent().get(0).getCompletionCount());
        assertEquals(75.0, result.getContent().get(0).getAverageProgress());
    }
    
    @Test
    @DisplayName("获取机构课程学习统计 - 测试方法逻辑")
    void testGetInstitutionCourseStatisticsLogic() {
        // 模拟查询结果
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courses = Arrays.asList(course1, course2);
        Page<Course> coursePage = new PageImpl<>(courses);
        
        when(courseRepository.findByInstitution(any(Institution.class), any(Pageable.class)))
                .thenReturn(coursePage);
        
        when(learningRecordRepository.findLearningStatsByCourseForInstitution(1L))
                .thenReturn(Arrays.asList(
                        new Object[]{1L, "测试课程1", 3600L, 10},
                        new Object[]{2L, "测试课程2", 1800L, 5}
                ));
        
        // 设置正确的返回结构
        Object[] learnerCount = new Object[]{20L};
        when(userCourseRepository.countLearnersByCourseId(anyLong()))
                .thenReturn(Collections.singletonList(learnerCount));
        
        when(userCourseRepository.countByProgress(anyLong(), anyInt())).thenReturn(10L);
        when(userCourseRepository.getAverageProgressByCourseId(anyLong())).thenReturn(75.0);
        
        // 执行方法两次
        Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> result1 = 
                statisticsService.getInstitutionCourseStatistics(1L, pageable);
        Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> result2 = 
                statisticsService.getInstitutionCourseStatistics(1L, pageable);
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        
        // 验证方法调用 - 由于缓存在测试环境不会生效，所以会调用两次
        verify(courseRepository, times(2)).findByInstitution(any(Institution.class), any(Pageable.class));
        verify(learningRecordRepository, times(2)).findLearningStatsByCourseForInstitution(1L);
    }

    @Test
    @DisplayName("获取机构最活跃用户")
    void testGetMostActiveUsers() {
        // 模拟查询结果
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{1L, "user1", 5400L, 15},
                new Object[]{2L, "user2", 3600L, 10}
        );
        
        when(learningRecordRepository.findMostActiveUsersByInstitution(eq(1L), any(Pageable.class)))
                .thenReturn(mockResults);
        
        // 执行方法
        List<InstitutionLearningStatisticsVO.ActiveUserVO> result = statisticsService.getMostActiveUsers(1L, 10);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals(5400L, result.get(0).getLearningDuration());
        assertEquals(15, result.get(0).getActivityCount());
    }
    
    @Test
    @DisplayName("获取机构最活跃用户 - 测试方法逻辑")
    void testGetMostActiveUsersLogic() {
        // 模拟查询结果
        List<Object[]> mockResults = Arrays.asList(
                new Object[]{1L, "user1", 5400L, 15},
                new Object[]{2L, "user2", 3600L, 10}
        );
        
        when(learningRecordRepository.findMostActiveUsersByInstitution(eq(1L), any(Pageable.class)))
                .thenReturn(mockResults);
        
        // 执行方法两次
        List<InstitutionLearningStatisticsVO.ActiveUserVO> result1 = statisticsService.getMostActiveUsers(1L, 10);
        List<InstitutionLearningStatisticsVO.ActiveUserVO> result2 = statisticsService.getMostActiveUsers(1L, 10);
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        
        // 验证方法调用 - 由于缓存在测试环境不会生效，所以会调用两次
        verify(learningRecordRepository, times(2)).findMostActiveUsersByInstitution(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("获取机构今日学习时长")
    void testGetInstitutionTodayLearningDuration() {
        when(learningRecordRepository.findTodayLearningDurationByInstitution(1L))
                .thenReturn(3600L);
        
        Long result = statisticsService.getInstitutionTodayLearningDuration(1L);
        
        assertEquals(3600L, result);
    }
    
    @Test
    @DisplayName("获取机构今日学习时长 - 测试方法逻辑")
    void testGetInstitutionTodayLearningDurationLogic() {
        when(learningRecordRepository.findTodayLearningDurationByInstitution(1L))
                .thenReturn(3600L);
        
        // 执行方法两次
        Long result1 = statisticsService.getInstitutionTodayLearningDuration(1L);
        Long result2 = statisticsService.getInstitutionTodayLearningDuration(1L);
        
        // 验证结果
        assertEquals(3600L, result1);
        assertEquals(3600L, result2);
        
        // 验证方法调用 - 由于缓存在测试环境不会生效，所以会调用两次
        verify(learningRecordRepository, times(2)).findTodayLearningDurationByInstitution(1L);
    }

    @Test
    @DisplayName("获取机构总学习时长")
    void testGetInstitutionTotalLearningDuration() {
        when(learningRecordRepository.findTotalLearningDurationByInstitution(1L))
                .thenReturn(7200L);
        
        Long result = statisticsService.getInstitutionTotalLearningDuration(1L);
        
        assertEquals(7200L, result);
    }

    @Test
    @DisplayName("获取机构学习人数")
    void testGetInstitutionLearnerCount() {
        when(learningRecordRepository.countUniqueUsersByInstitution(1L))
                .thenReturn(25L);
        
        Long result = statisticsService.getInstitutionLearnerCount(1L);
        
        assertEquals(25L, result);
    }
} 