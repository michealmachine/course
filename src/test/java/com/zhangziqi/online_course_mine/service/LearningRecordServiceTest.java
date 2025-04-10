package com.zhangziqi.online_course_mine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordEndDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordStartDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.LearningRecordVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.impl.LearningRecordServiceImpl;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LearningRecordServiceTest {

    @Mock
    private LearningRecordRepository learningRecordRepository;

    @Mock
    private UserCourseRepository userCourseRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private ChapterRepository chapterRepository;
    
    @Mock
    private SectionRepository sectionRepository;
    
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LearningRecordServiceImpl learningRecordService;

    private User user;
    private Course course;
    private LearningRecord record1;
    private LearningRecord record2;
    private LearningRecordVO recordVO1;
    private LearningRecordVO recordVO2;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();

        course = Course.builder()
                .id(1L)
                .title("测试课程")
                .build();

        record1 = LearningRecord.builder()
                .id(1L)
                .user(user)
                .course(course)
                .activityType(LearningActivityType.VIDEO_WATCH.getCode())
                .activityStartTime(LocalDateTime.now().minusHours(2))
                .activityEndTime(LocalDateTime.now().minusHours(1))
                .durationSeconds(3600)
                .build();

        record2 = LearningRecord.builder()
                .id(2L)
                .user(user)
                .course(course)
                .activityType(LearningActivityType.QUIZ_ATTEMPT.getCode())
                .activityStartTime(LocalDateTime.now().minusHours(5))
                .activityEndTime(LocalDateTime.now().minusHours(4))
                .durationSeconds(3600)
                .build();
                
        // 创建对应的VO对象
        recordVO1 = new LearningRecordVO();
        recordVO1.setId(1L);
        recordVO1.setUserId(user.getId());
        recordVO1.setCourseId(course.getId());
        
        recordVO2 = new LearningRecordVO();
        recordVO2.setId(2L);
        recordVO2.setUserId(user.getId());
        recordVO2.setCourseId(course.getId());
    }

    @Test
    @DisplayName("启动学习活动 - 用户已购买课程状态正常")
    void testStartActivity_WithValidUserCourse() {
        // 设置模拟行为 - 用户已购买课程且状态为NORMAL
        when(userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(user.getId(), course.getId(), UserCourseStatus.NORMAL.getValue()))
                .thenReturn(true);
                
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        
        // 模拟chapterRepository和sectionRepository的行为
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        when(learningRecordRepository.findByUser_IdAndActivityEndTimeIsNull(anyLong()))
                .thenReturn(Optional.empty());
        
        when(learningRecordRepository.save(any(LearningRecord.class)))
                .thenAnswer(invocation -> {
                    LearningRecord savedRecord = invocation.getArgument(0);
                    savedRecord.setId(3L);
                    return savedRecord;
                });

        // 创建DTO
        LearningRecordStartDTO dto = new LearningRecordStartDTO();
        dto.setCourseId(course.getId());
        dto.setChapterId(1L);
        dto.setSectionId(1L);
        dto.setActivityType(LearningActivityType.VIDEO_WATCH.getCode());
        
        // 执行方法
        LearningRecordVO result = learningRecordService.startActivity(user.getId(), dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertEquals(course.getId(), result.getCourseId());
        assertEquals(LearningActivityType.VIDEO_WATCH.getCode(), result.getActivityType());
        
        // 验证仓库方法调用
        verify(userCourseRepository).existsByUser_IdAndCourse_IdAndStatus(
                user.getId(), course.getId(), UserCourseStatus.NORMAL.getValue());
        verify(learningRecordRepository).save(any(LearningRecord.class));
    }

    @Test
    @DisplayName("启动学习活动 - 用户未购买或课程状态异常")
    void testStartActivity_WithInvalidUserCourse() {
        // 设置模拟行为 - 用户未购买课程或课程状态不为NORMAL
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(user.getId(), course.getId(), UserCourseStatus.NORMAL.getValue()))
                .thenReturn(false);

        // 创建DTO
        LearningRecordStartDTO dto = new LearningRecordStartDTO();
        dto.setCourseId(course.getId());
        dto.setActivityType(LearningActivityType.VIDEO_WATCH.getCode());

        // 执行方法并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            learningRecordService.startActivity(user.getId(), dto);
        });

        // 验证异常消息
        assertEquals("请先购买课程再进行学习，或检查课程是否已过期或退款", exception.getMessage());
        
        // 验证仓库方法调用
        verify(userCourseRepository).existsByUser_IdAndCourse_IdAndStatus(
                user.getId(), course.getId(), UserCourseStatus.NORMAL.getValue());
        verify(learningRecordRepository, never()).save(any(LearningRecord.class));
    }

    @Test
    @DisplayName("结束学习活动")
    void testEndActivity() {
        // 设置模拟行为
        record1.setActivityEndTime(null); // 确保记录未结束
        when(learningRecordRepository.findById(1L)).thenReturn(Optional.of(record1));
        when(learningRecordRepository.save(any(LearningRecord.class))).thenReturn(record1);
        
        // 创建DTO
        LearningRecordEndDTO dto = new LearningRecordEndDTO();
        dto.setContextData("{\"progress\":75}");

        // 执行方法
        LearningRecordVO result = learningRecordService.endActivity(user.getId(), 1L, dto);

        // 验证结果
        assertNotNull(result);
        
        // 验证仓库方法调用
        verify(learningRecordRepository).findById(1L);
        verify(learningRecordRepository).save(any(LearningRecord.class));
    }

    @Test
    @DisplayName("结束学习活动 - 记录不存在")
    void testEndActivity_RecordNotFound() {
        // 设置模拟行为
        when(learningRecordRepository.findById(99L)).thenReturn(Optional.empty());
        
        // 创建DTO
        LearningRecordEndDTO dto = new LearningRecordEndDTO();

        // 执行方法并验证异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            learningRecordService.endActivity(user.getId(), 99L, dto);
        });

        // 验证异常消息含有"不存在"信息
        assertTrue(exception.getMessage().contains("不存在"), "错误消息应包含'不存在'");
        
        // 验证仓库方法调用
        verify(learningRecordRepository).findById(99L);
        verify(learningRecordRepository, never()).save(any(LearningRecord.class));
    }

    @Test
    @DisplayName("获取用户学习记录")
    void testGetUserLearningRecords() {
        // 设置模拟行为
        Pageable pageable = PageRequest.of(0, 10);
        List<LearningRecord> records = Arrays.asList(record1, record2);
        Page<LearningRecord> page = new PageImpl<>(records, pageable, records.size());
        
        when(learningRecordRepository.findByUser_IdOrderByActivityStartTimeDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);
                
        // 执行方法
        Page<LearningRecordVO> result = learningRecordService.getUserActivities(1L, pageable);

        // 验证结果
        assertNotNull(result);
        
        // 验证仓库方法调用
        verify(learningRecordRepository).findByUser_IdOrderByActivityStartTimeDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("获取用户课程学习记录")
    void testGetUserCourseLearningRecords() {
        // 设置模拟行为
        Pageable pageable = PageRequest.of(0, 10);
        List<LearningRecord> records = Arrays.asList(record1, record2);
        Page<LearningRecord> page = new PageImpl<>(records, pageable, records.size());
        
        when(learningRecordRepository.findByUser_IdAndCourse_IdOrderByActivityStartTimeDesc(
                eq(1L), eq(1L), any(Pageable.class))).thenReturn(page);

        // 执行方法
        Page<LearningRecordVO> result = learningRecordService.getUserCourseActivities(1L, 1L, pageable);

        // 验证结果
        assertNotNull(result);
        
        // 验证仓库方法调用
        verify(learningRecordRepository).findByUser_IdAndCourse_IdOrderByActivityStartTimeDesc(
                eq(1L), eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("查找进行中的学习活动")
    void testFindOngoingActivity() {
        // 设置模拟行为
        LearningRecord ongoingRecord = LearningRecord.builder()
                .id(3L)
                .user(user)
                .course(course)
                .activityType(LearningActivityType.VIDEO_WATCH.getCode())
                .activityStartTime(LocalDateTime.now().minusMinutes(30))
                .build();
        
        when(learningRecordRepository.findByUser_IdAndActivityEndTimeIsNull(1L))
                .thenReturn(Optional.of(ongoingRecord));

        // 执行方法
        Optional<LearningRecordVO> result = learningRecordService.findOngoingActivity(1L);

        // 验证结果
        assertTrue(result.isPresent());
        
        // 验证仓库方法调用
        verify(learningRecordRepository).findByUser_IdAndActivityEndTimeIsNull(1L);
    }
} 