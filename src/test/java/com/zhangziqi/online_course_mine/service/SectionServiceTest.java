package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.section.*;
import com.zhangziqi.online_course_mine.model.entity.*;
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
    private SectionResourceDTO testSectionResourceDTO;
    private SectionQuestionGroupDTO testSectionQuestionGroupDTO;

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
                .build();

        // 创建测试媒体
        testMedia = Media.builder()
                .id(1L)
                .title("测试媒体")
                .description("这是一个测试媒体")
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
                .questionGroup(testQuestionGroup)
                .sectionId(testSection.getId())
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

        // 创建测试小节资源DTO
        testSectionResourceDTO = SectionResourceDTO.builder()
                .sectionId(testSection.getId())
                .mediaId(testMedia.getId())
                .resourceType("primary")
                .orderIndex(0)
                .build();

        // 创建测试小节题目组DTO
        testSectionQuestionGroupDTO = SectionQuestionGroupDTO.builder()
                .sectionId(testSection.getId())
                .questionGroupId(testQuestionGroup.getId())
                .orderIndex(0)
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
        lenient().when(sectionRepository.findMaxOrderIndexByChapter_Id(anyLong())).thenReturn(null);
        when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> {
            Section savedSection = invocation.getArgument(0);
            savedSection.setId(1L);
            return savedSection;
        });

        // 执行测试
        Section result = sectionService.createSection(testSectionCreateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testSectionCreateDTO.getTitle(), result.getTitle());
        assertEquals(testSectionCreateDTO.getDescription(), result.getDescription());
        assertEquals(testChapter, result.getChapter());
        assertEquals(testSectionCreateDTO.getOrderIndex(), result.getOrderIndex());
        assertEquals(testSectionCreateDTO.getContentType(), result.getContentType());

        // 验证调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    @DisplayName("创建小节 - 章节不存在")
    void createSection_ChapterNotFound() {
        // 准备测试数据
        when(chapterRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            sectionService.createSection(testSectionCreateDTO);
        });

        // 验证异常消息
        assertTrue(exception.getMessage().contains("章节不存在"));

        // 验证调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(sectionRepository, never()).save(any(Section.class));
    }

    @Test
    @DisplayName("更新小节 - 成功")
    void updateSection_Success() {
        // 准备测试数据
        SectionCreateDTO updateDTO = SectionCreateDTO.builder()
                .title("更新后的小节")
                .description("这是更新后的小节描述")
                .chapterId(testChapter.getId())
                .orderIndex(1)
                .contentType("document")
                .build();

        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);

        // 执行测试
        Section result = sectionService.updateSection(testSection.getId(), updateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(updateDTO.getTitle(), result.getTitle());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertEquals(updateDTO.getOrderIndex(), result.getOrderIndex());
        assertEquals(updateDTO.getContentType(), result.getContentType());

        // 验证调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionRepository).save(testSection);
    }

    @Test
    @DisplayName("获取小节 - 成功")
    void getSectionById_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));

        // 执行测试
        Section result = sectionService.getSectionById(testSection.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testSection.getId(), result.getId());
        assertEquals(testSection.getTitle(), result.getTitle());

        // 验证调用
        verify(sectionRepository).findById(testSection.getId());
    }

    @Test
    @DisplayName("获取小节 - 小节不存在")
    void getSectionById_SectionNotFound() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            sectionService.getSectionById(99L);
        });

        // 验证异常消息
        assertTrue(exception.getMessage().contains("小节不存在"));

        // 验证调用
        verify(sectionRepository).findById(99L);
    }

    @Test
    @DisplayName("获取章节小节列表 - 成功")
    void getSectionsByChapter_Success() {
        // 准备测试数据
        List<Section> sectionList = Arrays.asList(testSection);

        when(chapterRepository.findById(anyLong())).thenReturn(Optional.of(testChapter));
        when(sectionRepository.findByChapter_IdOrderByOrderIndexAsc(anyLong())).thenReturn(sectionList);

        // 执行测试
        List<Section> result = sectionService.getSectionsByChapter(testChapter.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSection.getId(), result.get(0).getId());

        // 验证调用
        verify(chapterRepository).findById(testChapter.getId());
        verify(sectionRepository).findByChapter_IdOrderByOrderIndexAsc(testChapter.getId());
    }

    @Test
    @DisplayName("删除小节 - 成功")
    void deleteSection_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        doNothing().when(sectionResourceRepository).deleteBySection_Id(anyLong());
        doNothing().when(sectionQuestionGroupRepository).deleteBySectionId(anyLong());
        doNothing().when(sectionRepository).delete(any(Section.class));

        // 执行测试
        sectionService.deleteSection(testSection.getId());

        // 验证调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionResourceRepository).deleteBySection_Id(testSection.getId());
        verify(sectionQuestionGroupRepository).deleteBySectionId(testSection.getId());
        verify(sectionRepository).delete(testSection);
    }

    @Test
    @DisplayName("添加小节资源 - 成功")
    void addSectionResource_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(mediaRepository.findById(anyLong())).thenReturn(Optional.of(testMedia));
        lenient().when(sectionResourceRepository.findBySection_Id(anyLong())).thenReturn(new ArrayList<>());
        when(sectionResourceRepository.save(any(SectionResource.class))).thenReturn(testSectionResource);

        // 执行测试
        SectionResource result = sectionService.addSectionResource(testSectionResourceDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testSectionResource.getId(), result.getId());
        assertEquals(testSection, result.getSection());
        assertEquals(testMedia, result.getMedia());
        assertEquals(testSectionResourceDTO.getResourceType(), result.getResourceType());
        assertEquals(testSectionResourceDTO.getOrderIndex(), result.getOrderIndex());

        // 验证调用
        verify(sectionRepository).findById(testSection.getId());
        verify(mediaRepository).findById(testMedia.getId());
        verify(sectionResourceRepository).save(any(SectionResource.class));
    }

    @Test
    @DisplayName("添加小节题目组 - 成功")
    void addSectionQuestionGroup_Success() {
        // 准备测试数据
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(questionGroupRepository.findById(anyLong())).thenReturn(Optional.of(testQuestionGroup));
        lenient().when(sectionQuestionGroupRepository.findBySectionIdOrderByOrderIndexAsc(anyLong())).thenReturn(new ArrayList<>());
        when(sectionQuestionGroupRepository.save(any(SectionQuestionGroup.class))).thenReturn(testSectionQuestionGroup);

        // 执行测试
        SectionQuestionGroup result = sectionService.addSectionQuestionGroup(testSectionQuestionGroupDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testSectionQuestionGroup.getId(), result.getId());
        assertEquals(testSection.getId(), result.getSectionId());
        assertEquals(testQuestionGroup, result.getQuestionGroup());
        assertEquals(testSectionQuestionGroupDTO.getOrderIndex(), result.getOrderIndex());
        assertEquals(testSectionQuestionGroupDTO.getRandomOrder(), result.getRandomOrder());
        assertEquals(testSectionQuestionGroupDTO.getOrderByDifficulty(), result.getOrderByDifficulty());
        assertEquals(testSectionQuestionGroupDTO.getShowAnalysis(), result.getShowAnalysis());

        // 验证调用
        verify(sectionRepository).findById(testSection.getId());
        verify(questionGroupRepository).findById(testQuestionGroup.getId());
        verify(sectionQuestionGroupRepository).save(any(SectionQuestionGroup.class));
    }

    @Test
    @DisplayName("获取小节资源列表 - 成功")
    void getSectionResources_Success() {
        // 准备测试数据
        List<SectionResource> resourceList = Arrays.asList(testSectionResource);

        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(sectionResourceRepository.findBySection_Id(anyLong())).thenReturn(resourceList);

        // 执行测试
        List<SectionResource> result = sectionService.getSectionResources(testSection.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSectionResource.getId(), result.get(0).getId());

        // 验证调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionResourceRepository).findBySection_Id(testSection.getId());
    }

    @Test
    @DisplayName("获取小节题目组列表 - 成功")
    void getSectionQuestionGroups_Success() {
        // 准备测试数据
        List<SectionQuestionGroup> questionGroupList = Arrays.asList(testSectionQuestionGroup);

        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(testSection));
        when(sectionQuestionGroupRepository.findBySectionIdOrderByOrderIndexAsc(anyLong())).thenReturn(questionGroupList);

        // 执行测试
        List<SectionQuestionGroup> result = sectionService.getSectionQuestionGroups(testSection.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSectionQuestionGroup.getId(), result.get(0).getId());

        // 验证调用
        verify(sectionRepository).findById(testSection.getId());
        verify(sectionQuestionGroupRepository).findBySectionIdOrderByOrderIndexAsc(testSection.getId());
    }

    @Test
    @DisplayName("删除小节资源 - 成功")
    void deleteSectionResource_Success() {
        // 准备测试数据
        when(sectionResourceRepository.findById(anyLong())).thenReturn(Optional.of(testSectionResource));
        doNothing().when(sectionResourceRepository).delete(any(SectionResource.class));

        // 执行测试
        sectionService.deleteSectionResource(testSectionResource.getId());

        // 验证调用
        verify(sectionResourceRepository).findById(testSectionResource.getId());
        verify(sectionResourceRepository).delete(testSectionResource);
    }

    @Test
    @DisplayName("删除小节题目组 - 成功")
    void deleteSectionQuestionGroup_Success() {
        // 准备测试数据
        when(sectionQuestionGroupRepository.findBySectionIdAndQuestionGroupId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testSectionQuestionGroup));
        doNothing().when(sectionQuestionGroupRepository).delete(any(SectionQuestionGroup.class));

        // 执行测试
        sectionService.deleteSectionQuestionGroup(testSection.getId(), testQuestionGroup.getId());

        // 验证调用
        verify(sectionQuestionGroupRepository).findBySectionIdAndQuestionGroupId(testSection.getId(), testQuestionGroup.getId());
        verify(sectionQuestionGroupRepository).delete(testSectionQuestionGroup);
    }
} 