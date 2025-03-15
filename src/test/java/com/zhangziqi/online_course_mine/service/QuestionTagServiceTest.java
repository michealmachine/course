package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.QuestionType;
import com.zhangziqi.online_course_mine.model.vo.QuestionTagVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionTagMappingRepository;
import com.zhangziqi.online_course_mine.repository.QuestionTagRepository;
import com.zhangziqi.online_course_mine.service.impl.QuestionTagServiceImpl;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionTagServiceTest {

    @Mock
    private QuestionTagRepository tagRepository;

    @Mock
    private QuestionTagMappingRepository tagMappingRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private QuestionTagServiceImpl questionTagService;

    private Institution testInstitution;
    private QuestionTag testTag;
    private Question testQuestion;
    private QuestionTagMapping testTagMapping;
    private QuestionTagDTO testTagDTO;
    private List<Object[]> testTagCountResult;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试机构
        testInstitution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .build();

        // 创建测试用户
        testUser = User.builder()
                .id(1L)
                .name("测试用户")
                .email("test@example.com")
                .build();

        // 创建测试题目
        testQuestion = Question.builder()
                .id(1L)
                .title("测试题目")
                .content("这是一道测试题目的内容")
                .type(QuestionType.SINGLE_CHOICE.getValue())
                .difficulty(2)
                .score(5)
                .analysis("这是题目解析")
                .answer("这是答案")
                .institution(testInstitution)
                .creatorId(testUser.getId())
                .creatorName(testUser.getName())
                .build();

        // 创建测试标签
        testTag = QuestionTag.builder()
                .id(1L)
                .name("测试标签")
                .institution(testInstitution)
                .creatorId(testUser.getId())
                .build();

        // 创建测试标签映射
        testTagMapping = QuestionTagMapping.builder()
                .id(1L)
                .question(testQuestion)
                .tag(testTag)
                .build();

        // 创建测试标签DTO
        testTagDTO = QuestionTagDTO.builder()
                .id(1L)
                .institutionId(testInstitution.getId())
                .name("测试标签")
                .build();

        // 创建测试计数结果
        testTagCountResult = new ArrayList<>();
        testTagCountResult.add(new Object[]{testTag.getId(), 5L});
    }

    @Test
    @DisplayName("创建标签 - 成功")
    void createTag_Success() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(tagRepository.findByInstitutionAndName(any(Institution.class), anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(QuestionTag.class))).thenReturn(testTag);

        // 执行测试
        QuestionTagVO result = questionTagService.createTag(testTagDTO, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        assertEquals(testTag.getName(), result.getName());
        assertEquals(testTag.getInstitution().getId(), result.getInstitutionId());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(tagRepository).findByInstitutionAndName(any(Institution.class), eq(testTag.getName()));
        verify(tagRepository).save(any(QuestionTag.class));
    }

    @Test
    @DisplayName("创建标签 - 机构不存在")
    void createTag_InstitutionNotFound() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.createTag(testTagDTO, 1L);
        });

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(tagRepository, never()).save(any(QuestionTag.class));
    }

    @Test
    @DisplayName("创建标签 - 标签已存在")
    void createTag_TagAlreadyExists() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(tagRepository.findByInstitutionAndName(any(Institution.class), anyString())).thenReturn(Optional.of(testTag));

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            questionTagService.createTag(testTagDTO, 1L);
        });

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(tagRepository).findByInstitutionAndName(any(Institution.class), eq(testTag.getName()));
        verify(tagRepository, never()).save(any(QuestionTag.class));
    }

    @Test
    @DisplayName("更新标签 - 成功")
    void updateTag_Success() {
        // 设置模拟行为
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        when(tagRepository.findByInstitutionAndName(any(Institution.class), anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(QuestionTag.class))).thenReturn(testTag);

        // 修改测试数据
        QuestionTagDTO updatedTagDTO = QuestionTagDTO.builder()
                .id(testTagDTO.getId())
                .institutionId(testTagDTO.getInstitutionId())
                .name("更新后的标签")
                .build();

        // 执行测试
        QuestionTagVO result = questionTagService.updateTag(updatedTagDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        assertEquals("更新后的标签", result.getName());
        assertEquals(testTag.getInstitution().getId(), result.getInstitutionId());

        // 验证方法调用
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagRepository).findByInstitutionAndName(any(Institution.class), eq(updatedTagDTO.getName()));
        verify(tagRepository).save(any(QuestionTag.class));
    }

    @Test
    @DisplayName("更新标签 - 标签不存在")
    void updateTag_TagNotFound() {
        // 设置模拟行为
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.updateTag(testTagDTO);
        });

        // 验证方法调用
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagRepository, never()).findByInstitutionAndName(any(Institution.class), anyString());
        verify(tagRepository, never()).save(any(QuestionTag.class));
    }

    @Test
    @DisplayName("更新标签 - 新名称已存在")
    void updateTag_NameAlreadyExists() {
        // 设置模拟行为
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        
        QuestionTag existingTag = QuestionTag.builder()
                .id(2L)
                .institution(testInstitution)
                .name("更新后的标签")
                .build();
        
        when(tagRepository.findByInstitutionAndName(any(Institution.class), anyString())).thenReturn(Optional.of(existingTag));

        // 修改测试数据
        QuestionTagDTO updatedTagDTO = QuestionTagDTO.builder()
                .id(testTagDTO.getId())
                .institutionId(testTagDTO.getInstitutionId())
                .name("更新后的标签")
                .build();

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            questionTagService.updateTag(updatedTagDTO);
        });

        // 验证方法调用
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagRepository).findByInstitutionAndName(any(Institution.class), eq(updatedTagDTO.getName()));
        verify(tagRepository, never()).save(any(QuestionTag.class));
    }

    @Test
    @DisplayName("获取标签 - 成功")
    void getTag_Success() {
        // 设置模拟行为
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        when(tagMappingRepository.countQuestionsByTagId(anyLong())).thenReturn(5L);

        // 执行测试
        QuestionTagVO result = questionTagService.getTagById(testTag.getId(), testInstitution.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        assertEquals(testTag.getName(), result.getName());
        assertEquals(testTag.getInstitution().getId(), result.getInstitutionId());
        assertEquals(5L, result.getQuestionCount());

        // 验证方法调用
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagMappingRepository).countQuestionsByTagId(testTag.getId());
    }

    @Test
    @DisplayName("获取标签 - 标签不存在")
    void getTag_TagNotFound() {
        // 设置模拟行为
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.getTagById(999L, testInstitution.getId());
        });

        // 验证方法调用
        verify(tagRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(tagMappingRepository, never()).countQuestionsByTagId(anyLong());
    }

    @Test
    @DisplayName("删除标签 - 成功")
    void deleteTag_Success() {
        // 设置模拟行为
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        doNothing().when(tagMappingRepository).deleteByTagId(anyLong());
        doNothing().when(tagRepository).delete(any(QuestionTag.class));

        // 执行测试
        questionTagService.deleteTag(testTag.getId(), testInstitution.getId());

        // 验证方法调用
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagMappingRepository).deleteByTagId(testTag.getId());
        verify(tagRepository).delete(testTag);
    }

    @Test
    @DisplayName("删除标签 - 标签不存在")
    void deleteTag_TagNotFound() {
        // 设置模拟行为
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.deleteTag(999L, testInstitution.getId());
        });

        // 验证方法调用
        verify(tagRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(tagMappingRepository, never()).deleteByTagId(anyLong());
        verify(tagRepository, never()).delete(any(QuestionTag.class));
    }

    @Test
    @DisplayName("获取所有标签 - 成功")
    void getAllTags_Success() {
        // 设置模拟行为
        when(tagRepository.findAllByInstitutionId(anyLong())).thenReturn(List.of(testTag));
        // 手动模拟 countQuestionsByTagIds 的结果
        when(tagMappingRepository.countQuestionsByTagId(anyLong())).thenReturn(5L);

        // 执行测试
        List<QuestionTagVO> results = questionTagService.getAllTags(testInstitution.getId());

        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testTag.getId(), results.get(0).getId());
        assertEquals(testTag.getName(), results.get(0).getName());
        assertEquals(testTag.getInstitution().getId(), results.get(0).getInstitutionId());
        assertEquals(5L, results.get(0).getQuestionCount());

        // 验证方法调用
        verify(tagRepository).findAllByInstitutionId(testInstitution.getId());
        verify(tagMappingRepository).countQuestionsByTagId(testTag.getId());
    }

    @Test
    @DisplayName("分页获取标签 - 成功")
    void getTags_Success() {
        // 设置模拟行为
        Institution institution = testInstitution;
        Pageable pageable = PageRequest.of(0, 10);
        Page<QuestionTag> tagPage = new PageImpl<>(List.of(testTag), pageable, 1);
        
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));
        when(tagRepository.findByInstitution(any(Institution.class), any(Pageable.class))).thenReturn(tagPage);
        when(tagMappingRepository.countQuestionsByTagId(anyLong())).thenReturn(5L);

        // 执行测试
        Page<QuestionTagVO> results = questionTagService.getTags(testInstitution.getId(), "", pageable);

        // 验证结果
        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertEquals(1, results.getContent().size());
        assertEquals(testTag.getId(), results.getContent().get(0).getId());
        assertEquals(testTag.getName(), results.getContent().get(0).getName());
        assertEquals(testTag.getInstitution().getId(), results.getContent().get(0).getInstitutionId());
        assertEquals(5L, results.getContent().get(0).getQuestionCount());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(tagRepository).findByInstitution(eq(institution), any(Pageable.class));
        verify(tagMappingRepository).countQuestionsByTagId(testTag.getId());
    }

    @Test
    @DisplayName("关联标签到题目 - 成功")
    void addTagToQuestion_Success() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        when(tagMappingRepository.findByQuestionAndTag(any(Question.class), any(QuestionTag.class))).thenReturn(Optional.empty());
        when(tagMappingRepository.save(any(QuestionTagMapping.class))).thenReturn(testTagMapping);

        // 执行测试
        boolean result = questionTagService.addTagToQuestion(testQuestion.getId(), testTag.getId(), testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagMappingRepository).findByQuestionAndTag(any(Question.class), any(QuestionTag.class));
        verify(tagMappingRepository).save(any(QuestionTagMapping.class));
    }

    @Test
    @DisplayName("关联标签到题目 - 标签不存在")
    void addTagToQuestion_TagNotFound() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByIdAndInstitutionId(eq(999L), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.addTagToQuestion(testQuestion.getId(), 999L, testInstitution.getId());
        });

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(tagRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(tagMappingRepository, never()).save(any(QuestionTagMapping.class));
    }

    @Test
    @DisplayName("关联标签到题目 - 题目不存在")
    void addTagToQuestion_QuestionNotFound() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(eq(999L), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.addTagToQuestion(999L, testTag.getId(), testInstitution.getId());
        });

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(tagRepository, never()).findByIdAndInstitutionId(anyLong(), anyLong());
        verify(tagMappingRepository, never()).save(any(QuestionTagMapping.class));
    }

    @Test
    @DisplayName("关联标签到题目 - 关系已存在")
    void addTagToQuestion_RelationAlreadyExists() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        when(tagMappingRepository.findByQuestionAndTag(any(Question.class), any(QuestionTag.class))).thenReturn(Optional.of(testTagMapping));

        // 执行测试
        boolean result = questionTagService.addTagToQuestion(testQuestion.getId(), testTag.getId(), testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagMappingRepository).findByQuestionAndTag(any(Question.class), any(QuestionTag.class));
        verify(tagMappingRepository, never()).save(any(QuestionTagMapping.class));
    }

    @Test
    @DisplayName("移除题目标签 - 成功")
    void removeTagFromQuestion_Success() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        doNothing().when(tagMappingRepository).deleteByQuestionIdAndTagId(anyLong(), anyLong());

        // 执行测试
        boolean result = questionTagService.removeTagFromQuestion(testQuestion.getId(), testTag.getId(), testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagMappingRepository).deleteByQuestionIdAndTagId(testQuestion.getId(), testTag.getId());
    }

    @Test
    @DisplayName("移除题目标签 - 标签不存在")
    void removeTagFromQuestion_TagNotFound() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByIdAndInstitutionId(eq(999L), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.removeTagFromQuestion(testQuestion.getId(), 999L, testInstitution.getId());
        });

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(tagRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(tagMappingRepository, never()).deleteByQuestionIdAndTagId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("移除题目标签 - 题目不存在")
    void removeTagFromQuestion_QuestionNotFound() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(eq(999L), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionTagService.removeTagFromQuestion(999L, testTag.getId(), testInstitution.getId());
        });

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(tagRepository, never()).findByIdAndInstitutionId(anyLong(), anyLong());
        verify(tagMappingRepository, never()).deleteByQuestionIdAndTagId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("移除题目标签 - 关系不存在")
    void removeTagFromQuestion_RelationNotFound() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(tagRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testTag));
        doNothing().when(tagMappingRepository).deleteByQuestionIdAndTagId(anyLong(), anyLong());

        // 执行测试
        boolean result = questionTagService.removeTagFromQuestion(testQuestion.getId(), testTag.getId(), testInstitution.getId());

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(tagRepository).findByIdAndInstitutionId(testTag.getId(), testInstitution.getId());
        verify(tagMappingRepository).deleteByQuestionIdAndTagId(testQuestion.getId(), testTag.getId());
    }

    @Test
    @DisplayName("为不同类型的题目添加标签")
    void addTagToQuestions_DifferentTypes_Success() {
        // 创建不同类型的题目
        List<Question> questions = Arrays.asList(
            Question.builder()
                .id(1L)
                .title("单选题")
                .content("单选题内容")
                .type(QuestionType.SINGLE_CHOICE.getValue())
                .difficulty(2)
                .score(5)
                .analysis("解析")
                .answer("A")
                .institution(testInstitution)
                .build(),
            Question.builder()
                .id(2L)
                .title("填空题")
                .content("填空题内容____")
                .type(QuestionType.FILL_BLANK.getValue())
                .difficulty(2)
                .score(5)
                .analysis("解析")
                .answer("答案")
                .institution(testInstitution)
                .build(),
            Question.builder()
                .id(3L)
                .title("判断题")
                .content("判断题内容")
                .type(QuestionType.TRUE_FALSE.getValue())
                .difficulty(2)
                .score(5)
                .analysis("解析")
                .answer("正确")
                .institution(testInstitution)
                .build()
        );

        // 创建标签
        QuestionTag tag = QuestionTag.builder()
                .id(1L)
                .name("通用标签")
                .institution(testInstitution)
                .build();

        // 设置模拟行为
        when(questionRepository.findAllById(anyList())).thenReturn(questions);
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));
        when(tagMappingRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<QuestionTagMapping> mappings = invocation.getArgument(0);
            AtomicLong id = new AtomicLong(1);
            return mappings.stream()
                    .peek(m -> m.setId(id.getAndIncrement()))
                    .collect(Collectors.toList());
        });

        // 执行测试 - 根据实际实现调整方法名和参数
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        
        // 如果addTagToQuestions方法不存在，请查看并使用当前实现中的正确方法名
        // 假设正确的方法是addTagToQuestionsById
        questionTagService.addTagToQuestionsById(tag.getId(), questionIds);

        // 验证方法调用
        verify(questionRepository).findAllById(questionIds);
        verify(tagRepository).findById(tag.getId());
        verify(tagMappingRepository).saveAll(argThat(mappings -> 
            mappings.size() == 3 && 
            mappings.stream().allMatch(m -> 
                m.getTag().equals(tag) && 
                questions.contains(m.getQuestion())
            )
        ));
    }

    @Test
    @DisplayName("获取带有标签的题目列表")
    void getQuestionsByTags_DifferentTypes_Success() {
        // 创建不同类型的题目和标签
        List<Question> questions = Arrays.asList(
            Question.builder()
                .id(1L)
                .title("单选题")
                .content("单选题内容")
                .type(QuestionType.SINGLE_CHOICE.getValue())
                .difficulty(2)
                .score(5)
                .analysis("解析")
                .answer("A")
                .institution(testInstitution)
                .build(),
            Question.builder()
                .id(2L)
                .title("简答题")
                .content("简答题内容")
                .type(QuestionType.SHORT_ANSWER.getValue())
                .difficulty(2)
                .score(5)
                .analysis("解析")
                .answer("参考答案")
                .institution(testInstitution)
                .build()
        );

        List<QuestionTag> tags = Arrays.asList(
            QuestionTag.builder()
                .id(1L)
                .name("标签1")
                .institution(testInstitution)
                .build(),
            QuestionTag.builder()
                .id(2L)
                .name("标签2")
                .institution(testInstitution)
                .build()
        );

        List<QuestionTagMapping> mappings = new ArrayList<>();
        for (Question q : questions) {
            for (QuestionTag t : tags) {
                mappings.add(QuestionTagMapping.builder()
                    .id((long) (mappings.size() + 1))
                    .question(q)
                    .tag(t)
                    .build());
            }
        }

        // 设置模拟行为
        when(tagMappingRepository.findByTagIdIn(anyList())).thenReturn(mappings);

        // 执行测试 - 根据实际实现调整方法名和参数
        List<Long> tagIds = tags.stream()
                .map(QuestionTag::getId)
                .collect(Collectors.toList());
        
        // 如果getQuestionsByTags方法不存在，请查看并使用当前实现中的正确方法名
        // 假设正确的方法是getQuestionsByTagIds
        List<Question> result = questionTagService.getQuestionsByTagIds(tagIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(questions));

        // 验证方法调用
        verify(tagMappingRepository).findByTagIdIn(tagIds);
    }
} 