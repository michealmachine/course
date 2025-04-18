package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.config.CacheConfig;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.LearningStatisticsVO;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.repository.UserWrongQuestionRepository;
import com.zhangziqi.online_course_mine.service.impl.LearningStatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LearningStatisticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCourseRepository userCourseRepository;

    @Mock
    private UserWrongQuestionRepository userWrongQuestionRepository;

    @Mock
    private WrongQuestionService wrongQuestionService;

    @InjectMocks
    private LearningStatisticsServiceImpl learningStatisticsService;

    private User testUser;
    private Course testCourse;
    private UserCourse testUserCourse;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .coverImage("test-cover.jpg")
                .build();

        testUserCourse = UserCourse.builder()
                .id(1L)
                .user(testUser)
                .course(testCourse)
                .progress(50)
                .status(UserCourseStatus.NORMAL.getValue())
                .learnDuration(3600)
                .lastLearnAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("获取用户学习统计数据 - 成功")
    void getUserLearningStatistics_Success() {
        // 准备测试数据
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userCourseRepository.findByUser_IdAndStatus(eq(1L), eq(UserCourseStatus.NORMAL.getValue())))
                .thenReturn(Collections.singletonList(testUserCourse));
        when(wrongQuestionService.countUserWrongQuestions(1L)).thenReturn(10L);
        when(wrongQuestionService.countUserUnresolvedWrongQuestions(1L)).thenReturn(5L);
        
        // 执行方法
        LearningStatisticsVO result = learningStatisticsService.getUserLearningStatistics(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1, result.getTotalCourses());
        assertEquals(0, result.getCompletedCourses()); // 进度50%，未完成
        assertEquals(3600, result.getTotalLearningDuration());
        assertEquals(10, result.getWrongQuestions());
        
        // 验证方法调用
        verify(userRepository).existsById(1L);
        verify(userCourseRepository).findByUser_IdAndStatus(1L, UserCourseStatus.NORMAL.getValue());
        verify(wrongQuestionService).countUserWrongQuestions(1L);
        verify(wrongQuestionService).countUserUnresolvedWrongQuestions(1L);
    }
    
    @Test
    @DisplayName("获取用户学习统计数据 - 测试方法逻辑")
    void getUserLearningStatistics_Logic() {
        // 准备测试数据
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userCourseRepository.findByUser_IdAndStatus(eq(1L), eq(UserCourseStatus.NORMAL.getValue())))
                .thenReturn(Collections.singletonList(testUserCourse));
        when(wrongQuestionService.countUserWrongQuestions(1L)).thenReturn(10L);
        when(wrongQuestionService.countUserUnresolvedWrongQuestions(1L)).thenReturn(5L);
        
        // 执行方法两次
        LearningStatisticsVO result1 = learningStatisticsService.getUserLearningStatistics(1L);
        LearningStatisticsVO result2 = learningStatisticsService.getUserLearningStatistics(1L);
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        
        // 验证数据库方法调用次数 - 由于缓存在测试环境不会生效，所以会调用两次
        verify(userRepository, times(2)).existsById(1L);
        verify(userCourseRepository, times(2)).findByUser_IdAndStatus(1L, UserCourseStatus.NORMAL.getValue());
        verify(wrongQuestionService, times(2)).countUserWrongQuestions(1L);
    }
    
    @Test
    @DisplayName("获取用户课程学习统计数据 - 成功")
    void getUserCourseLearningStatistics_Success() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(eq(1L), eq(1L)))
                .thenReturn(Optional.of(testUserCourse));
        
        // 执行方法
        LearningStatisticsVO.CourseStatisticsVO result = 
                learningStatisticsService.getUserCourseLearningStatistics(1L, 1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getCourseId());
        assertEquals("测试课程", result.getCourseTitle());
        assertEquals("test-cover.jpg", result.getCourseCover());
        assertEquals(50, result.getProgress());
        assertEquals(3600L, result.getLearningDuration());
        assertNotNull(result.getLastLearnTime());
        
        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(1L, 1L);
    }
    
    @Test
    @DisplayName("获取用户课程学习统计数据 - 测试方法逻辑")
    void getUserCourseLearningStatistics_Logic() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(eq(1L), eq(1L)))
                .thenReturn(Optional.of(testUserCourse));
        
        // 执行方法两次
        LearningStatisticsVO.CourseStatisticsVO result1 = 
                learningStatisticsService.getUserCourseLearningStatistics(1L, 1L);
        LearningStatisticsVO.CourseStatisticsVO result2 = 
                learningStatisticsService.getUserCourseLearningStatistics(1L, 1L);
        
        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        
        // 验证数据库方法调用次数 - 由于缓存在测试环境不会生效，所以会调用两次
        verify(userCourseRepository, times(2)).findByUser_IdAndCourse_Id(1L, 1L);
    }
    
    @Test
    @DisplayName("重置用户课程学习进度 - 成功")
    void resetUserCourseProgress_Success() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(eq(1L), eq(1L)))
                .thenReturn(Optional.of(testUserCourse));
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(testUserCourse);
        
        // 执行方法
        learningStatisticsService.resetUserCourseProgress(1L, 1L);
        
        // 验证结果
        assertEquals(0, testUserCourse.getProgress());
        assertNull(testUserCourse.getCurrentChapterId());
        assertNull(testUserCourse.getCurrentSectionId());
        assertEquals(0, testUserCourse.getCurrentSectionProgress());
        
        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(1L, 1L);
        verify(userCourseRepository).save(testUserCourse);
    }
    
    @Test
    @DisplayName("重置用户课程学习进度 - 课程不存在")
    void resetUserCourseProgress_CourseNotFound() {
        // 准备测试数据
        when(userCourseRepository.findByUser_IdAndCourse_Id(eq(1L), eq(999L)))
                .thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> learningStatisticsService.resetUserCourseProgress(1L, 999L));
        
        assertEquals("未找到学习记录", exception.getMessage());
        
        // 验证方法调用
        verify(userCourseRepository).findByUser_IdAndCourse_Id(1L, 999L);
        verify(userCourseRepository, never()).save(any(UserCourse.class));
    }
} 