package com.zhangziqi.online_course_mine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordCompletedDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordEndDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordStartDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.DateLearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningRecordVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.impl.LearningRecordServiceImpl;
import com.zhangziqi.online_course_mine.service.impl.RedisLearningRecordService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Mock
    private RedisLearningRecordService redisLearningRecordService;

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

    @Test
    @DisplayName("获取用户学习热力图数据")
    void testGetUserLearningHeatmap() {
        // 设置模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // 模拟热力图数据
        List<Object[]> heatmapData = new ArrayList<>();
        heatmapData.add(new Object[]{1, 9, 300}); // 周一9点学习时长300秒
        heatmapData.add(new Object[]{2, 14, 180}); // 周二14点学习时长180秒
        heatmapData.add(new Object[]{3, 20, 420}); // 周三20点学习时长420秒

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(learningRecordRepository.findLearningHeatmapDataByUser(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(heatmapData);

        // 执行方法
        LearningHeatmapVO result = learningRecordService.getUserLearningHeatmap(1L, startDate, endDate);

        // 验证结果
        assertNotNull(result);
        assertNull(result.getCourseId()); // 用户总体热力图没有特定课程
        assertEquals(420, result.getMaxActivityCount()); // 最大学习时长应为420秒

        // 验证热力图数据
        Map<Integer, Map<Integer, Integer>> heatmap = result.getHeatmapData();
        assertEquals(3, heatmap.size()); // 应有3个工作日的数据

        // 验证周一9点的数据
        assertEquals(300, heatmap.get(1).get(9));

        // 验证周二14点的数据
        assertEquals(180, heatmap.get(2).get(14));

        // 验证周三20点的数据
        assertEquals(420, heatmap.get(3).get(20));

        // 验证仓库方法调用
        verify(userRepository).findById(1L);
        verify(learningRecordRepository).findLearningHeatmapDataByUser(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("获取用户按日期分组的学习热力图数据")
    void testGetUserLearningHeatmapByDate() {
        // 设置模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // 模拟按日期分组的热力图数据
        List<Object[]> heatmapData = new ArrayList<>();
        heatmapData.add(new Object[]{"2023-06-01", 300}); // 2023-06-01学习时长300秒
        heatmapData.add(new Object[]{"2023-06-02", 180}); // 2023-06-02学习时长180秒
        heatmapData.add(new Object[]{"2023-06-03", 420}); // 2023-06-03学习时长420秒

        LocalDate startDate = LocalDate.of(2023, 6, 1);
        LocalDate endDate = LocalDate.of(2023, 6, 3);

        when(learningRecordRepository.findLearningHeatmapDataByUserGroupByDate(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(heatmapData);

        // 执行方法
        DateLearningHeatmapVO result = learningRecordService.getUserLearningHeatmapByDate(1L, startDate, endDate);

        // 验证结果
        assertNotNull(result);
        assertNull(result.getCourseId()); // 用户总体热力图没有特定课程
        assertEquals(420, result.getMaxActivityCount()); // 最大学习时长应为420秒

        // 验证热力图数据
        Map<String, Integer> heatmap = result.getHeatmapData();
        assertEquals(3, heatmap.size()); // 应有3天的数据

        // 验证各日期的数据
        assertEquals(300, heatmap.get("2023-06-01").intValue());
        assertEquals(180, heatmap.get("2023-06-02").intValue());
        assertEquals(420, heatmap.get("2023-06-03").intValue());

        // 验证仓库方法调用
        verify(userRepository).findById(1L);
        verify(learningRecordRepository).findLearningHeatmapDataByUserGroupByDate(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("记录已完成学习活动 - 使用Redis")
    void testRecordCompletedActivity_WithRedis() {
        // 设置模拟行为
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(
                user.getId(), course.getId(), UserCourseStatus.NORMAL.getValue()))
                .thenReturn(true);

        // 创建DTO
        LearningRecordCompletedDTO dto = new LearningRecordCompletedDTO();
        dto.setCourseId(course.getId());
        dto.setChapterId(1L);
        dto.setSectionId(1L);
        dto.setActivityType(LearningActivityType.VIDEO_WATCH.getCode());
        dto.setDurationSeconds(120); // 2分钟
        dto.setContextData("{\"progress\":50}");

        // 执行方法
        LearningRecordVO result = learningRecordService.recordCompletedActivity(user.getId(), dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertEquals(course.getId(), result.getCourseId());
        assertEquals(LearningActivityType.VIDEO_WATCH.getCode(), result.getActivityType());
        assertEquals(120, result.getDurationSeconds());
        assertEquals("{\"progress\":50}", result.getContextData());

        // 验证Redis服务调用
        verify(redisLearningRecordService).updateLearningRecord(
                eq(user.getId()),
                eq(course.getId()),
                eq(1L),
                eq(1L),
                eq(LearningActivityType.VIDEO_WATCH.getCode()),
                eq(120),
                eq("{\"progress\":50}"));

        // 验证没有调用数据库保存
        verify(learningRecordRepository, never()).save(any(LearningRecord.class));
    }
}