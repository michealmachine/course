package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.chapter.ChapterCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.chapter.ChapterOrderDTO;
import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.enums.ChapterAccessType;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.repository.ChapterRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.service.impl.ChapterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ChapterServiceImpl chapterService;

    private Institution testInstitution;
    private Course testCourse;
    private Chapter testChapter;
    private ChapterCreateDTO testChapterCreateDTO;

    @BeforeEach
    void setUp() {
        // 创建测试机构
        testInstitution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .build();

        // 创建测试课程
        testCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .description("这是一个测试课程")
                .institution(testInstitution)
                .status(CourseStatus.DRAFT.getValue())
                .build();

        // 创建测试章节
        testChapter = Chapter.builder()
                .id(1L)
                .title("测试章节")
                .description("这是一个测试章节")
                .course(testCourse)
                .orderIndex(0)
                .accessType(ChapterAccessType.PAID_ONLY.getValue())
                .estimatedMinutes(30)
                .build();

        // 创建测试章节创建DTO
        testChapterCreateDTO = ChapterCreateDTO.builder()
                .title("测试章节")
                .description("这是一个测试章节")
                .courseId(testCourse.getId())
                .orderIndex(0)
                .accessType(ChapterAccessType.PAID_ONLY.getValue())
                .estimatedMinutes(30)
                .build();
    }

    @Test
    @DisplayName("创建章节 - 成功")
    void createChapter_Success() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        // 使用lenient()来标记这个存根，这样即使方法没被调用也不会报错
        lenient().when(chapterRepository.findMaxOrderIndexByCourse_Id(anyLong())).thenReturn(null);
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> {
            Chapter savedChapter = invocation.getArgument(0);
            savedChapter.setId(1L);
            return savedChapter;
        });
        
        // 必须mock这个方法，因为它在updateCourseTotalLessonsAndDuration中被调用
        when(chapterRepository.findByCourse_IdOrderByOrderIndexAsc(anyLong())).thenReturn(Arrays.asList(testChapter));

        // 执行测试
        Chapter result = chapterService.createChapter(testChapterCreateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testChapterCreateDTO.getTitle(), result.getTitle());
        assertEquals(testChapterCreateDTO.getDescription(), result.getDescription());
        assertEquals(testCourse, result.getCourse());
        assertEquals(testChapterCreateDTO.getOrderIndex(), result.getOrderIndex());
        assertEquals(testChapterCreateDTO.getAccessType(), result.getAccessType());
        assertEquals(testChapterCreateDTO.getEstimatedMinutes(), result.getEstimatedMinutes());

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
        // 移除不需要严格验证的方法调用验证
        // verify(chapterRepository, lenient()).findMaxOrderIndexByCourse_Id(testCourse.getId());
        verify(chapterRepository).save(any(Chapter.class));
        verify(chapterRepository).findByCourse_IdOrderByOrderIndexAsc(testCourse.getId());
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("创建章节 - 课程不存在")
    void createChapter_CourseNotFound() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            chapterService.createChapter(testChapterCreateDTO);
        });

        // 验证异常消息
        assertTrue(exception.getMessage().contains("课程不存在"));

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    @DisplayName("更新章节 - 成功")
    void updateChapter_Success() {
        // 准备测试数据
        ChapterCreateDTO updateDTO = ChapterCreateDTO.builder()
                .title("更新后的章节")
                .description("这是更新后的章节描述")
                .courseId(testCourse.getId())
                .orderIndex(1)
                .accessType(ChapterAccessType.FREE_TRIAL.getValue())
                .estimatedMinutes(45)
                .build();

        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        when(chapterRepository.save(any(Chapter.class))).thenReturn(testChapter);

        // 执行测试
        Chapter result = chapterService.updateChapter(testChapter.getId(), updateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(updateDTO.getTitle(), result.getTitle());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getOrderIndex(), result.getOrderIndex());
        assertEquals(updateDTO.getAccessType(), result.getAccessType());
        assertEquals(updateDTO.getEstimatedMinutes(), result.getEstimatedMinutes());

        // 验证调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(chapterRepository).save(testChapter);
        verify(chapterRepository).findByCourse_IdOrderByOrderIndexAsc(testCourse.getId());
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("获取章节 - 成功")
    void getChapterById_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));

        // 执行测试
        Chapter result = chapterService.getChapterById(testChapter.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testChapter.getId(), result.getId());
        assertEquals(testChapter.getTitle(), result.getTitle());

        // 验证调用
        verify(chapterRepository).findById(testChapter.getId());
    }

    @Test
    @DisplayName("获取章节 - 章节不存在")
    void getChapterById_ChapterNotFound() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            chapterService.getChapterById(99L);
        });

        // 验证异常消息
        assertTrue(exception.getMessage().contains("章节不存在"));

        // 验证调用
        verify(chapterRepository).findById(99L);
    }

    @Test
    @DisplayName("获取课程章节列表 - 成功")
    void getChaptersByCourse_Success() {
        // 准备测试数据
        List<Chapter> chapterList = Arrays.asList(testChapter);

        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(chapterRepository.findByCourse_IdOrderByOrderIndexAsc(anyLong())).thenReturn(chapterList);

        // 执行测试
        List<Chapter> result = chapterService.getChaptersByCourse(testCourse.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testChapter.getId(), result.get(0).getId());

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
        verify(chapterRepository).findByCourse_IdOrderByOrderIndexAsc(testCourse.getId());
    }

    @Test
    @DisplayName("删除章节 - 成功")
    void deleteChapter_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        doNothing().when(chapterRepository).delete(any(Chapter.class));

        // 执行测试
        chapterService.deleteChapter(testChapter.getId());

        // 验证调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(chapterRepository).delete(testChapter);
        verify(chapterRepository).findByCourse_IdOrderByOrderIndexAsc(testCourse.getId());
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("更新章节访问类型 - 成功")
    void updateAccessType_Success() {
        // 准备测试数据
        Integer newAccessType = ChapterAccessType.FREE_TRIAL.getValue();

        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        when(chapterRepository.save(any(Chapter.class))).thenReturn(testChapter);

        // 执行测试
        Chapter result = chapterService.updateAccessType(testChapter.getId(), newAccessType);

        // 验证结果
        assertNotNull(result);
        assertEquals(newAccessType, result.getAccessType());

        // 验证调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(chapterRepository).save(testChapter);
    }

    @Test
    @DisplayName("重新排序章节 - 成功")
    void reorderChapters_Success() {
        // 准备测试数据
        List<Chapter> chapterList = new ArrayList<>();
        Chapter chapter1 = Chapter.builder().id(1L).title("章节1").course(testCourse).orderIndex(0).build();
        Chapter chapter2 = Chapter.builder().id(2L).title("章节2").course(testCourse).orderIndex(1).build();
        chapterList.add(chapter1);
        chapterList.add(chapter2);

        List<ChapterOrderDTO> orderDTOs = new ArrayList<>();
        orderDTOs.add(ChapterOrderDTO.builder().id(1L).orderIndex(1).build());
        orderDTOs.add(ChapterOrderDTO.builder().id(2L).orderIndex(0).build());

        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(chapterRepository.findByCourse_IdOrderByOrderIndexAsc(anyLong())).thenReturn(chapterList);
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        List<Chapter> result = chapterService.reorderChapters(testCourse.getId(), orderDTOs);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
        // 使用atLeastOnce()因为这个方法在实现中被调用了多次
        verify(chapterRepository, atLeastOnce()).findByCourse_IdOrderByOrderIndexAsc(testCourse.getId());
        verify(chapterRepository, times(2)).save(any(Chapter.class));
    }
} 