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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.InputStream;

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

    @Mock
    private MinioService minioService;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Institution testInstitution;
    private Category testCategory;
    private Tag testTag;
    private Course testCourse;
    private CourseCreateDTO testCourseCreateDTO;
    private Long testCreatorId = 1L;

    // MultipartFile模拟
    @Mock
    private MultipartFile mockFile;

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
        Course result = courseService.createCourse(testCourseCreateDTO, testCreatorId, testInstitution.getId());

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
            courseService.createCourse(testCourseCreateDTO, testCreatorId, testInstitution.getId());
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
            courseService.createCourse(testCourseCreateDTO, testCreatorId, testInstitution.getId());
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
        Course result = courseService.updateCourse(testCourse.getId(), updateDTO, testInstitution.getId());

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

    @Test
    @DisplayName("更新课程封面 - 成功")
    void updateCourseCover_Success() throws IOException {
        // 准备测试数据
        Course draftCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .description("这是一个测试课程")
                .institution(testInstitution)
                .status(CourseStatus.DRAFT.getValue())
                .coverImage("http://localhost:8999/media/course-covers/1/old-image.jpg")
                .build();
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(draftCourse));
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024 * 1024L); // 1MB
        when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(minioService.uploadFile(anyString(), any(InputStream.class), anyString())).thenReturn("http://localhost:8999/media/course-covers/1/some-uuid-test-image.jpg");
        
        // 使用lenient()标记可能不会被调用的模拟
        lenient().when(minioService.deleteFile(anyString())).thenReturn(true);
        
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // 执行测试
        Course result = courseService.updateCourseCover(draftCourse.getId(), mockFile);
        
        // 验证结果
        assertNotNull(result);
        assertNotEquals("http://localhost:8999/media/course-covers/1/old-image.jpg", result.getCoverImage());
        assertTrue(result.getCoverImage().contains("course-covers/1/"));
        
        // 验证调用
        verify(courseRepository).findById(draftCourse.getId());
        verify(mockFile, atLeastOnce()).getContentType();  // 允许多次调用
        verify(mockFile).getSize();
        verify(mockFile).getInputStream();
        verify(minioService).uploadFile(anyString(), any(InputStream.class), anyString());
        // 不验证deleteFile方法，因为它可能被调用也可能不被调用
        verify(courseRepository).save(draftCourse);
    }

    @Test
    @DisplayName("更新课程封面 - 课程状态不允许")
    void updateCourseCover_InvalidStatus() {
        // 准备测试数据 - 使用PUBLISHED状态的课程
        Course publishedCourse = Course.builder()
                .id(1L)
                .title("已发布课程")
                .description("这是一个已发布的课程")
                .institution(testInstitution)
                .status(CourseStatus.PUBLISHED.getValue())
                .build();
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(publishedCourse));
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.updateCourseCover(publishedCourse.getId(), mockFile);
        });
        
        // 验证异常消息
        assertTrue(exception.getMessage().contains("只有草稿或已拒绝状态的课程才能更新封面"));
        
        // 验证调用
        verify(courseRepository).findById(publishedCourse.getId());
        verify(mockFile, never()).getContentType();
        verify(minioService, never()).uploadFile(anyString(), any(InputStream.class), anyString());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("更新课程封面 - 文件类型不支持")
    void updateCourseCover_UnsupportedFileType() {
        // 准备测试数据
        Course draftCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .description("这是一个测试课程")
                .institution(testInstitution)
                .status(CourseStatus.DRAFT.getValue())
                .build();
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(draftCourse));
        when(mockFile.getContentType()).thenReturn("application/pdf"); // 不支持的文件类型
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.updateCourseCover(draftCourse.getId(), mockFile);
        });
        
        // 验证异常消息
        assertTrue(exception.getMessage().contains("只支持上传图片文件"));
        
        // 验证调用
        verify(courseRepository).findById(draftCourse.getId());
        verify(mockFile).getContentType();
        verify(minioService, never()).uploadFile(anyString(), any(InputStream.class), anyString());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("更新课程封面 - 文件大小超限")
    void updateCourseCover_FileTooLarge() {
        // 准备测试数据
        Course draftCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .description("这是一个测试课程")
                .institution(testInstitution)
                .status(CourseStatus.DRAFT.getValue())
                .build();
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(draftCourse));
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(10 * 1024 * 1024L); // 10MB, 超过5MB限制
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.updateCourseCover(draftCourse.getId(), mockFile);
        });
        
        // 验证异常消息
        assertTrue(exception.getMessage().contains("文件大小不能超过5MB"));
        
        // 验证调用
        verify(courseRepository).findById(draftCourse.getId());
        verify(mockFile).getContentType();
        verify(mockFile).getSize();
        verify(minioService, never()).uploadFile(anyString(), any(InputStream.class), anyString());
        verify(courseRepository, never()).save(any(Course.class));
    }
} 