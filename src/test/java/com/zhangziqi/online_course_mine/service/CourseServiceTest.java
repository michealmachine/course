package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.course.CourseCreateDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.CourseVersion;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.PreviewUrlVO;
import com.zhangziqi.online_course_mine.model.vo.CourseStructureVO;
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
import org.springframework.data.jpa.domain.Specification;
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
                .category(testCategory)
                .creatorId(testCreatorId)
                .status(CourseStatus.DRAFT.getValue())
                .paymentType(CoursePaymentType.FREE.getValue())
                .build();
        testCourse.setTags(Set.of(testTag));

        // 创建测试课程创建DTO
        testCourseCreateDTO = CourseCreateDTO.builder()
                .title("测试课程")
                .description("这是一个测试课程")
                .categoryId(testCategory.getId())
                .tagIds(Set.of(testTag.getId()))
                .paymentType(CoursePaymentType.FREE.getValue())
                .build();
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
        
        // 执行方法
        CourseVO result = courseService.createCourse(testCourseCreateDTO, testCreatorId, testInstitution.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testCourseCreateDTO.getTitle(), result.getTitle());
        assertEquals(testCourseCreateDTO.getDescription(), result.getDescription());
        assertEquals(CourseStatus.DRAFT.getValue(), result.getStatus());
        assertEquals(CourseVersion.DRAFT.getValue(), result.getVersionType());
        assertEquals(testCreatorId, result.getCreatorId());
        assertEquals(testInstitution.getId(), result.getInstitution().getId());
        assertEquals(testCategory.getId(), result.getCategory().getId());
        assertEquals(1, result.getTags().size());
        
        // 验证方法调用
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
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> courseService.createCourse(testCourseCreateDTO, testCreatorId, testInstitution.getId()));
        
        assertTrue(exception.getMessage().contains("机构不存在"));
        
        // 验证方法调用
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
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> courseService.createCourse(testCourseCreateDTO, testCreatorId, testInstitution.getId()));
        
        assertTrue(exception.getMessage().contains("付费课程必须设置价格"));
        
        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("获取课程 - 成功")
    void getCourseById_Success() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        
        // 执行方法
        CourseVO result = courseService.getCourseById(testCourse.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getId());
        assertEquals(testCourse.getTitle(), result.getTitle());
        assertEquals(testCourse.getDescription(), result.getDescription());
        assertEquals(testCourse.getStatus(), result.getStatus());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
    }

    @Test
    @DisplayName("获取课程 - 课程不存在")
    void getCourseById_CourseNotFound() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> courseService.getCourseById(1L));
        
        assertTrue(exception.getMessage().contains("课程不存在"));
        
        // 验证方法调用
        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("获取机构课程列表 - 成功")
    void getCoursesByInstitution_Success() {
        // 准备测试数据
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courseList = List.of(testCourse);
        Page<Course> coursePage = new PageImpl<>(courseList, pageable, courseList.size());
        
        when(courseRepository.findByInstitution(any(Institution.class), any(Pageable.class))).thenReturn(coursePage);
        
        // 执行方法
        Page<CourseVO> result = courseService.getCoursesByInstitution(testInstitution.getId(), pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCourse.getId(), result.getContent().get(0).getId());
        assertEquals(testCourse.getTitle(), result.getContent().get(0).getTitle());
        
        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(courseRepository).findByInstitution(testInstitution, pageable);
    }

    @Test
    @DisplayName("更新课程 - 成功")
    void updateCourse_Success() {
        // 准备测试数据
        testCourse.setStatus(CourseStatus.DRAFT.getValue());
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        
        // 修改测试DTO数据
        testCourseCreateDTO.setTitle("更新后的课程");
        testCourseCreateDTO.setDescription("这是更新后的课程描述");
        
        // 执行方法
        CourseVO result = courseService.updateCourse(testCourse.getId(), testCourseCreateDTO, testInstitution.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testCourseCreateDTO.getTitle(), result.getTitle());
        assertEquals(testCourseCreateDTO.getDescription(), result.getDescription());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(institutionRepository).findById(testInstitution.getId());
        verify(categoryRepository).findById(testCategory.getId());
        verify(tagRepository).findById(testTag.getId());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("提交课程审核 - 成功")
    void submitForReview_Success() {
        // 准备测试数据
        testCourse.setStatus(CourseStatus.DRAFT.getValue());
        testCourse.setVersionType(CourseVersion.DRAFT.getValue());
        
        // 添加章节
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        chapter.setTitle("测试章节");
        chapter.setCourse(testCourse);
        testCourse.setChapters(List.of(chapter));
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        
        // 执行方法
        CourseVO result = courseService.submitForReview(testCourse.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(CourseStatus.PENDING_REVIEW.getValue(), result.getStatus());
        assertEquals(CourseVersion.REVIEW.getValue(), result.getVersionType());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("生成预览URL - 成功")
    void generatePreviewUrl_Success() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 执行方法
        PreviewUrlVO result = courseService.generatePreviewUrl(testCourse.getId());
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getUrl().contains("/api/courses/preview/"));
        assertEquals(testCourse.getId(), result.getCourseId());
        assertEquals(testCourse.getTitle(), result.getCourseTitle());
        assertNotNull(result.getExpireTime());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("更新课程封面 - 成功")
    void updateCourseCover_Success() throws IOException {
        // 准备测试数据
        testCourse.setStatus(CourseStatus.DRAFT.getValue());
        String imageUrl = "http://localhost:8999/media/course-covers/1/test.jpg";
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L); // 1KB
        when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(minioService.uploadFile(anyString(), any(InputStream.class), anyString())).thenReturn(imageUrl);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        
        // 执行方法
        CourseVO result = courseService.updateCourseCover(testCourse.getId(), mockFile);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getId());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(mockFile, atLeastOnce()).getContentType();
        verify(mockFile).getSize();
        verify(mockFile).getInputStream();
        verify(mockFile).getOriginalFilename();
        verify(minioService).uploadFile(anyString(), any(InputStream.class), anyString());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("更新课程封面 - 课程状态不允许")
    void updateCourseCover_InvalidStatus() {
        // 准备测试数据
        testCourse.setStatus(CourseStatus.PUBLISHED.getValue());
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> courseService.updateCourseCover(testCourse.getId(), mockFile));
        
        assertTrue(exception.getMessage().contains("只有草稿或已拒绝状态的课程才能更新封面"));
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(mockFile, never()).getContentType();
    }

    @Test
    @DisplayName("更新课程封面 - 文件类型不支持")
    void updateCourseCover_UnsupportedFileType() {
        // 准备测试数据
        testCourse.setStatus(CourseStatus.DRAFT.getValue());
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(mockFile.getContentType()).thenReturn("application/pdf");
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> courseService.updateCourseCover(testCourse.getId(), mockFile));
        
        assertTrue(exception.getMessage().contains("只支持上传图片文件"));
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(mockFile).getContentType();
        verify(mockFile, never()).getSize();
    }

    @Test
    @DisplayName("更新课程封面 - 文件大小超限")
    void updateCourseCover_FileTooLarge() throws IOException {
        // 准备测试数据
        testCourse.setStatus(CourseStatus.DRAFT.getValue());
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(6 * 1024 * 1024L); // 6MB
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> courseService.updateCourseCover(testCourse.getId(), mockFile));
        
        assertTrue(exception.getMessage().contains("文件大小不能超过5MB"));
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
        verify(mockFile).getContentType();
        verify(mockFile).getSize();
        verify(mockFile, never()).getInputStream();
    }

    @Test
    @DisplayName("获取指定状态的课程 - 成功")
    void getCoursesByStatus_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courseList = List.of(testCourse);
        Page<Course> coursePage = new PageImpl<>(courseList, pageable, courseList.size());
        
        when(courseRepository.findByStatus(anyInt(), any(Pageable.class))).thenReturn(coursePage);
        
        // 执行方法
        Page<CourseVO> result = courseService.getCoursesByStatus(CourseStatus.PENDING_REVIEW.getValue(), pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCourse.getId(), result.getContent().get(0).getId());
        
        // 验证方法调用
        verify(courseRepository).findByStatus(CourseStatus.PENDING_REVIEW.getValue(), pageable);
    }
    
    @Test
    @DisplayName("获取审核员正在审核的课程 - 成功")
    void getCoursesByStatusAndReviewer_Success() {
        // 准备测试数据
        Long reviewerId = 2L;
        testCourse.setReviewerId(reviewerId);
        testCourse.setStatus(CourseStatus.REVIEWING.getValue());
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> courseList = List.of(testCourse);
        Page<Course> coursePage = new PageImpl<>(courseList, pageable, courseList.size());
        
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(coursePage);
        
        // 执行方法
        Page<CourseVO> result = courseService.getCoursesByStatusAndReviewer(
                CourseStatus.REVIEWING.getValue(), reviewerId, pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCourse.getId(), result.getContent().get(0).getId());
        assertEquals(reviewerId, result.getContent().get(0).getReviewerId());
        assertEquals(CourseStatus.REVIEWING.getValue(), result.getContent().get(0).getStatus());
        
        // 验证方法调用
        verify(courseRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("获取课程结构 - 成功")
    void getCourseStructure_Success() {
        // 准备测试数据
        // 为课程添加章节和小节
        Chapter chapter = new Chapter();
        chapter.setId(1L);
        chapter.setTitle("测试章节");
        chapter.setDescription("这是一个测试章节");
        chapter.setOrderIndex(1);
        chapter.setCourse(testCourse);
        
        Section section = new Section();
        section.setId(1L);
        section.setTitle("测试小节");
        section.setDescription("这是一个测试小节");
        section.setOrderIndex(1);
        section.setContentType("video");
        section.setChapter(chapter);
        
        List<Section> sections = List.of(section);
        chapter.setSections(sections);
        
        List<Chapter> chapters = List.of(chapter);
        testCourse.setChapters(chapters);
        
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        
        // 执行方法
        CourseStructureVO result = courseService.getCourseStructure(testCourse.getId());
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getCourse());
        assertEquals(testCourse.getId(), result.getCourse().getId());
        assertEquals(testCourse.getTitle(), result.getCourse().getTitle());
        
        // 验证章节信息
        assertNotNull(result.getChapters());
        assertEquals(1, result.getChapters().size());
        assertEquals(chapter.getId(), result.getChapters().get(0).getId());
        assertEquals(chapter.getTitle(), result.getChapters().get(0).getTitle());
        
        // 验证小节信息
        assertNotNull(result.getChapters().get(0).getSections());
        assertEquals(1, result.getChapters().get(0).getSections().size());
        assertEquals(section.getId(), result.getChapters().get(0).getSections().get(0).getId());
        assertEquals(section.getTitle(), result.getChapters().get(0).getSections().get(0).getTitle());
        
        // 验证方法调用
        verify(courseRepository).findById(testCourse.getId());
    }
    
    @Test
    @DisplayName("获取课程结构 - 课程不存在")
    void getCourseStructure_CourseNotFound() {
        // 准备测试数据
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> courseService.getCourseStructure(1L));
        
        assertTrue(exception.getMessage().contains("课程不存在"));
        
        // 验证方法调用
        verify(courseRepository).findById(1L);
    }
} 