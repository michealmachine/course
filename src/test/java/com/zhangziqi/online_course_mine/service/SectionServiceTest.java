package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.section.*;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import com.zhangziqi.online_course_mine.model.vo.SectionVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.impl.SectionServiceImpl;
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
public class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private QuestionGroupRepository questionGroupRepository;

    @Mock
    private SectionResourceRepository sectionResourceRepository;

    @Mock
    private SectionQuestionGroupRepository sectionQuestionGroupRepository;

    @InjectMocks
    private SectionServiceImpl sectionService;

    private Institution testInstitution;
    private Course testCourse;
    private Chapter testChapter;
    private Section testSection;
    private Media testMedia;
    private QuestionGroup testQuestionGroup;
    private SectionResource testSectionResource;
    private SectionQuestionGroup testSectionQuestionGroup;
    private SectionCreateDTO testSectionCreateDTO;
    private SectionQuestionGroupConfigDTO testQuestionGroupConfigDTO;
    private String testResourceType = "primary";

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
                .build();

        // 创建测试章节
        testChapter = Chapter.builder()
                .id(1L)
                .title("测试章节")
                .description("这是一个测试章节")
                .course(testCourse)
                .orderIndex(0)
                .build();

        // 创建测试小节
        testSection = Section.builder()
                .id(1L)
                .title("测试小节")
                .description("这是一个测试小节")
                .chapter(testChapter)
                .orderIndex(0)
                .contentType("video")
                .resourceTypeDiscriminator("NONE")
                .build();

        // 创建测试媒体
        testMedia = Media.builder()
                .id(1L)
                .originalFilename("test-video.mp4")
                .type(MediaType.VIDEO)
                .size(1024 * 1024L)
                .uploaderId(1L)
                .title("测试视频")
                .build();

        // 创建测试题目组
        testQuestionGroup = QuestionGroup.builder()
                .id(1L)
                .name("测试题目组")
                .description("这是一个测试题目组")
                .build();

        // 创建测试小节资源
        testSectionResource = SectionResource.builder()
                .id(1L)
                .section(testSection)
                .media(testMedia)
                .resourceType("primary")
                .orderIndex(0)
                .build();

        // 创建测试小节题目组
        testSectionQuestionGroup = SectionQuestionGroup.builder()
                .id(1L)
                .sectionId(testSection.getId())
                .questionGroup(testQuestionGroup)
                .orderIndex(0)
                .randomOrder(false)
                .orderByDifficulty(false)
                .showAnalysis(true)
                .build();

        // 创建测试小节创建DTO
        testSectionCreateDTO = SectionCreateDTO.builder()
                .title("测试小节")
                .description("这是一个测试小节")
                .chapterId(testChapter.getId())
                .orderIndex(0)
                .contentType("video")
                .build();

        // 创建测试题目组配置DTO
        testQuestionGroupConfigDTO = SectionQuestionGroupConfigDTO.builder()
                .randomOrder(false)
                .orderByDifficulty(false)
                .showAnalysis(true)
                .build();
    }

    @Test
    @DisplayName("创建小节 - 成功")
    void createSection_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> {
            Section savedSection = invocation.getArgument(0);
            savedSection.setId(1L);
            return savedSection;
        });
        
        // 执行方法
        SectionVO result = sectionService.createSection(testSectionCreateDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testSectionCreateDTO.getTitle(), result.getTitle());
        assertEquals(testSectionCreateDTO.getDescription(), result.getDescription());
        assertEquals(testSectionCreateDTO.getOrderIndex(), result.getOrderIndex());
        assertEquals(testSectionCreateDTO.getContentType(), result.getContentType());
        
        // 验证方法调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("创建小节 - 章节不存在")
    void createSection_ChapterNotFound() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> sectionService.createSection(testSectionCreateDTO));
        
        assertTrue(exception.getMessage().contains("章节不存在"));
        
        // 验证方法调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(sectionRepository, never()).save(any(Section.class));
    }

    @Test
    @DisplayName("更新小节 - 成功")
    void updateSection_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        lenient().when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);
        
        // 修改DTO数据
        testSectionCreateDTO.setTitle("更新后的小节");
        testSectionCreateDTO.setDescription("这是更新后的小节描述");
        testSectionCreateDTO.setChapterId(testSection.getChapter().getId());
        
        // 执行方法
        SectionVO result = sectionService.updateSection(testSection.getId(), testSectionCreateDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testSectionCreateDTO.getTitle(), result.getTitle());
        assertEquals(testSectionCreateDTO.getDescription(), result.getDescription());
        
        // 验证方法调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("获取小节 - 成功")
    void getSectionById_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        
        // 执行方法
        SectionVO result = sectionService.getSectionById(testSection.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testSection.getId(), result.getId());
        assertEquals(testSection.getTitle(), result.getTitle());
        assertEquals(testSection.getDescription(), result.getDescription());
        
        // 验证方法调用
        verify(sectionRepository).findById(testSection.getId());
    }

    @Test
    @DisplayName("获取小节 - 小节不存在")
    void getSectionById_SectionNotFound() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> sectionService.getSectionById(99L));
        
        assertTrue(exception.getMessage().contains("小节不存在"));
        
        // 验证方法调用
        verify(sectionRepository).findById(99L);
    }

    @Test
    @DisplayName("获取章节小节列表 - 成功")
    void getSectionsByChapter_Success() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        when(sectionRepository.findByChapter_IdOrderByOrderIndexAsc(anyLong())).thenReturn(List.of(testSection));
        
        // 执行方法
        List<SectionVO> result = sectionService.getSectionsByChapter(testChapter.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSection.getId(), result.get(0).getId());
        assertEquals(testSection.getTitle(), result.get(0).getTitle());
        
        // 验证方法调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(sectionRepository).findByChapter_IdOrderByOrderIndexAsc(testChapter.getId());
    }

    @Test
    @DisplayName("删除小节 - 成功")
    void deleteSection_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        
        // 执行方法
        sectionService.deleteSection(testSection.getId());
        
        // 验证方法调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionResourceRepository).deleteBySection_Id(testSection.getId());
        verify(sectionQuestionGroupRepository).deleteBySectionId(testSection.getId());
        verify(sectionRepository).delete(testSection);
    }

    @Test
    @DisplayName("设置小节媒体资源 - 成功")
    void setMediaResource_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(mediaRepository.findById(anyLong())).thenReturn(Optional.of(testMedia));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);
        
        // 设置媒体资源相关状态
        testSection.setResourceTypeDiscriminator("MEDIA");
        testSection.setMedia(testMedia);
        testSection.setMediaResourceType(testResourceType);
        
        // 执行方法
        SectionVO result = sectionService.setMediaResource(testSection.getId(), testMedia.getId(), testResourceType);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("MEDIA", result.getResourceTypeDiscriminator());
        assertNotNull(result.getMedia());
        assertEquals(testMedia.getId(), result.getMediaId());
        assertEquals(testResourceType, result.getMediaResourceType());
        
        // 验证方法调用
        verify(sectionRepository).findById(testSection.getId());
        verify(mediaRepository).findById(testMedia.getId());
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("移除小节媒体资源 - 成功")
    void removeMediaResource_Success() {
        // 准备测试数据
        // 设置媒体资源相关状态
        testSection.setResourceTypeDiscriminator("MEDIA");
        testSection.setMedia(testMedia);
        testSection.setMediaResourceType(testResourceType);
        
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);
        
        // 执行方法
        SectionVO result = sectionService.removeMediaResource(testSection.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals("NONE", result.getResourceTypeDiscriminator());
        assertNull(result.getMedia());
        assertNull(result.getMediaId());
        
        // 验证方法调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("设置小节题目组 - 成功")
    void setQuestionGroup_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(questionGroupRepository.findById(anyLong())).thenReturn(Optional.of(testQuestionGroup));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);
        
        // 设置题目组相关状态
        testSection.setResourceTypeDiscriminator("QUESTION_GROUP");
        testSection.setQuestionGroup(testQuestionGroup);
        testSection.setRandomOrder(false);
        testSection.setOrderByDifficulty(false);
        testSection.setShowAnalysis(true);
        
        // 执行方法
        SectionVO result = sectionService.setQuestionGroup(testSection.getId(), testQuestionGroup.getId(), testQuestionGroupConfigDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("QUESTION_GROUP", result.getResourceTypeDiscriminator());
        assertNotNull(result.getQuestionGroup());
        assertEquals(testQuestionGroup.getId(), result.getQuestionGroupId());
        assertEquals(testQuestionGroupConfigDTO.getRandomOrder(), result.getRandomOrder());
        assertEquals(testQuestionGroupConfigDTO.getOrderByDifficulty(), result.getOrderByDifficulty());
        assertEquals(testQuestionGroupConfigDTO.getShowAnalysis(), result.getShowAnalysis());
        
        // 验证方法调用
        verify(sectionRepository).findById(testSection.getId());
        verify(questionGroupRepository).findById(testQuestionGroup.getId());
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("移除小节题目组 - 成功")
    void removeQuestionGroup_Success() {
        // 准备测试数据
        // 设置题目组相关状态
        testSection.setResourceTypeDiscriminator("QUESTION_GROUP");
        testSection.setQuestionGroup(testQuestionGroup);
        testSection.setRandomOrder(false);
        testSection.setOrderByDifficulty(false);
        testSection.setShowAnalysis(true);
        
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);
        
        // 执行方法
        SectionVO result = sectionService.removeQuestionGroup(testSection.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals("NONE", result.getResourceTypeDiscriminator());
        assertNull(result.getQuestionGroup());
        assertNull(result.getQuestionGroupId());
        
        // 验证方法调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionRepository).save(any(Section.class));
    }
}