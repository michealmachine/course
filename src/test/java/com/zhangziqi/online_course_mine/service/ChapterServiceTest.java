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
import com.zhangziqi.online_course_mine.model.vo.ChapterVO;
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
        
        // 执行方法
        ChapterVO result = chapterService.createChapter(testChapterCreateDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testChapterCreateDTO.getTitle(), result.getTitle());
        assertEquals(testChapterCreateDTO.getDescription(), result.getDescription());
        assertEquals(testChapterCreateDTO.getOrderIndex(), result.getOrderIndex());
        assertEquals(testChapterCreateDTO.getAccessType(), result.getAccessType());
        assertEquals(testCourse.getId(), result.getCourseId());
        assertEquals(testCourse.getTitle(), result.getCourseName());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(chapterRepository).save(any(Chapter.class));
    }

    @Test
    @DisplayName("创建章节 - 课程不存在")
    void createChapter_CourseNotFound() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> chapterService.createChapter(testChapterCreateDTO));
        
        assertTrue(exception.getMessage().contains("课程不存在"));
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(chapterRepository, never()).save(any(Chapter.class));
    }

    @Test
    @DisplayName("更新章节 - 成功")
    void updateChapter_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        lenient().when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(chapterRepository.save(any(Chapter.class))).thenReturn(testChapter);
        
        // 修改DTO数据
        testChapterCreateDTO.setTitle("更新后的章节");
        testChapterCreateDTO.setDescription("这是更新后的章节描述");
        
        // 执行方法
        ChapterVO result = chapterService.updateChapter(testChapter.getId(), testChapterCreateDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testChapterCreateDTO.getTitle(), result.getTitle());
        assertEquals(testChapterCreateDTO.getDescription(), result.getDescription());
        
        // 验证方法调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(chapterRepository).save(any(Chapter.class));
    }

    @Test
    @DisplayName("获取章节 - 成功")
    void getChapterById_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        
        // 执行方法
        ChapterVO result = chapterService.getChapterById(testChapter.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testChapter.getId(), result.getId());
        assertEquals(testChapter.getTitle(), result.getTitle());
        assertEquals(testChapter.getDescription(), result.getDescription());
        
        // 验证方法调用
        verify(chapterRepository).findById(testChapter.getId());
    }

    @Test
    @DisplayName("获取章节 - 章节不存在")
    void getChapterById_ChapterNotFound() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> chapterService.getChapterById(99L));
        
        assertTrue(exception.getMessage().contains("章节不存在"));
        
        // 验证方法调用
        verify(chapterRepository).findById(99L);
    }

    @Test
    @DisplayName("获取课程章节列表 - 成功")
    void getChaptersByCourse_Success() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(chapterRepository.findByCourse_IdOrderByOrderIndexAsc(anyLong())).thenReturn(List.of(testChapter));
        
        // 执行方法
        List<ChapterVO> result = chapterService.getChaptersByCourse(testCourse.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testChapter.getId(), result.get(0).getId());
        assertEquals(testChapter.getTitle(), result.get(0).getTitle());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(chapterRepository, atLeastOnce()).findByCourse_IdOrderByOrderIndexAsc(testCourse.getId());
    }

    @Test
    @DisplayName("删除章节 - 成功")
    void deleteChapter_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        
        // 执行方法
        chapterService.deleteChapter(testChapter.getId());
        
        // 验证方法调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(chapterRepository).delete(testChapter);
    }

    @Test
    @DisplayName("更新章节访问类型 - 成功")
    void updateAccessType_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        when(chapterRepository.save(any(Chapter.class))).thenReturn(testChapter);
        
        int newAccessType = ChapterAccessType.FREE_TRIAL.getValue();
        
        // 执行方法
        ChapterVO result = chapterService.updateAccessType(testChapter.getId(), newAccessType);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(newAccessType, result.getAccessType());
        
        // 验证方法调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(chapterRepository).save(any(Chapter.class));
    }

    @Test
    @DisplayName("重新排序章节 - 成功")
    void reorderChapters_Success() {
        // 准备测试数据
        Chapter chapter1 = Chapter.builder().id(1L).title("章节1").course(testCourse).orderIndex(0).build();
        Chapter chapter2 = Chapter.builder().id(2L).title("章节2").course(testCourse).orderIndex(1).build();
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(chapterRepository.findByCourse_IdOrderByOrderIndexAsc(anyLong())).thenReturn(List.of(chapter1, chapter2));
        
        List<ChapterOrderDTO> orderDTOs = Arrays.asList(
                new ChapterOrderDTO(2L, 0),
                new ChapterOrderDTO(1L, 1)
        );
        
        when(chapterRepository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // 执行方法
        List<ChapterVO> result = chapterService.reorderChapters(testCourse.getId(), orderDTOs);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(chapterRepository, atLeastOnce()).findByCourse_IdOrderByOrderIndexAsc(testCourse.getId());
        verify(chapterRepository, times(2)).save(any(Chapter.class));
    }
} 