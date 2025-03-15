package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.course.CourseCreateDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.CourseVersion;
import com.zhangziqi.online_course_mine.model.vo.PreviewUrlVO;
import com.zhangziqi.online_course_mine.repository.CategoryRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.TagRepository;
import com.zhangziqi.online_course_mine.service.impl.CourseServiceImpl;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Institution testInstitution;
    private Category testCategory;
    private Tag testTag;
    private Course testCourse;
    private CourseCreateDTO testCourseCreateDTO;
    private Long testCreatorId = 1L;

    @BeforeEach
    void setUp() {
        // 创建测试机构
        testInstitution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .build();

        // 创建测试分类
        testCategory = Category.builder()
                .id(1L)
                .name("测试分类")
                .build();

        // 创建测试标签
        testTag = Tag.builder()
                .id(1L)
                .name("测试标签")
                .build();

        // 创建测试课程
        testCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .description("这是一个测试课程")
                .institution(testInstitution)
                .status(CourseStatus.DRAFT.getValue())
                .build();

        // 创建测试课程创建DTO
        testCourseCreateDTO = CourseCreateDTO.builder()
                .title("测试课程")
                .description("这是一个测试课程")
                .institutionId(testInstitution.getId())
                .categoryId(testCategory.getId())
                .tagIds(new HashSet<>(Collections.singletonList(testTag.getId())))
                .paymentType(CoursePaymentType.FREE.getValue())
                .build();

        // 移除不必要的mock，避免UnnecessaryStubbingException
        // when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("创建课程 - 成功")
    void createCourse_Success() {
        // 准备测试数据
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setId(1L);
            return savedCourse;
        });

        // 执行测试
        Course result = courseService.createCourse(testCourseCreateDTO, testCreatorId);

        // 验证结果
        assertNotNull(result);
        assertEquals(testCourseCreateDTO.getTitle(), result.getTitle());
        assertEquals(testCourseCreateDTO.getDescription(), result.getDescription());
        assertEquals(testInstitution, result.getInstitution());
        assertEquals(testCategory, result.getCategory());
        assertTrue(result.getTags().contains(testTag));
        assertEquals(testCreatorId, result.getCreatorId());
        assertEquals(CourseStatus.DRAFT.getValue(), result.getStatus());
        assertEquals(CourseVersion.DRAFT.getValue(), result.getVersionType());
        assertEquals(false, result.getIsPublishedVersion());
        assertEquals(CoursePaymentType.FREE.getValue(), result.getPaymentType());

        // 验证调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(categoryRepository).findById(testCategory.getId());
        verify(tagRepository).findById(testTag.getId());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("创建课程 - 机构不存在")
    void createCourse_InstitutionNotFound() {
        // 准备测试数据
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseService.createCourse(testCourseCreateDTO, testCreatorId);
        });

        // 验证异常消息
        assertTrue(exception.getMessage().contains("机构不存在"));

        // 验证调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("创建课程 - 付费课程但未设置价格")
    void createCourse_PaidCourseWithoutPrice() {
        // 准备测试数据
        testCourseCreateDTO.setPaymentType(CoursePaymentType.PAID.getValue());
        testCourseCreateDTO.setPrice(null);

        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.createCourse(testCourseCreateDTO, testCreatorId);
        });

        // 验证异常消息
        assertTrue(exception.getMessage().contains("付费课程必须设置价格"));

        // 验证调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("获取课程 - 成功")
    void getCourseById_Success() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));

        // 执行测试
        Course result = courseService.getCourseById(testCourse.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getId());
        assertEquals(testCourse.getTitle(), result.getTitle());

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
    }

    @Test
    @DisplayName("获取课程 - 课程不存在")
    void getCourseById_CourseNotFound() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            courseService.getCourseById(99L);
        });

        // 验证异常消息
        assertTrue(exception.getMessage().contains("课程不存在"));

        // 验证调用
        verify(courseRepository).findById(99L);
    }

    @Test
    @DisplayName("获取机构课程列表 - 成功")
    void getCoursesByInstitution_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courseList = Collections.singletonList(testCourse);
        Page<Course> coursePage = new PageImpl<>(courseList, pageable, courseList.size());

        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(courseRepository.findByInstitution(any(Institution.class), any(Pageable.class))).thenReturn(coursePage);

        // 执行测试
        Page<Course> result = courseService.getCoursesByInstitution(testInstitution.getId(), pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCourse.getId(), result.getContent().get(0).getId());

        // 验证调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(courseRepository).findByInstitution(testInstitution, pageable);
    }

    @Test
    @DisplayName("更新课程 - 成功")
    void updateCourse_Success() {
        // 准备测试数据
        CourseCreateDTO updateDTO = CourseCreateDTO.builder()
                .title("更新后的课程")
                .description("这是更新后的课程描述")
                .institutionId(testInstitution.getId())
                .categoryId(testCategory.getId())
                .tagIds(new HashSet<>(Collections.singletonList(testTag.getId())))
                .paymentType(CoursePaymentType.FREE.getValue())
                .build();

        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // 执行测试
        Course result = courseService.updateCourse(testCourse.getId(), updateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(updateDTO.getTitle(), result.getTitle());
        assertEquals(updateDTO.getDescription(), result.getDescription());

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
        verify(institutionRepository).findById(testInstitution.getId());
        verify(categoryRepository).findById(testCategory.getId());
        verify(tagRepository).findById(testTag.getId());
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("提交课程审核 - 成功")
    void submitForReview_Success() {
        // 准备测试数据
        // 添加一个非空的章节列表，因为业务逻辑要求至少有一个章节
        List<Chapter> chapters = new ArrayList<>();
        chapters.add(Chapter.builder().id(1L).title("测试章节").build());
        testCourse.setChapters(chapters);
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // 执行测试
        Course result = courseService.submitForReview(testCourse.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(CourseStatus.PENDING_REVIEW.getValue(), result.getStatus());
        assertEquals(CourseVersion.REVIEW.getValue(), result.getVersionType());

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("生成预览URL - 成功")
    void generatePreviewUrl_Success() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // 执行测试
        PreviewUrlVO result = courseService.generatePreviewUrl(testCourse.getId());

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getUrl());
        assertNotNull(result.getExpireTime());
        assertEquals(testCourse.getId(), result.getCourseId());
        assertEquals(testCourse.getTitle(), result.getCourseTitle());

        // 验证调用
        verify(courseRepository).findById(testCourse.getId());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), eq(testCourse.getId().toString()), anyLong(), eq(TimeUnit.MINUTES));
    }
} 