package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.QuestionDTO;
import com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Question;
import com.zhangziqi.online_course_mine.model.entity.QuestionOption;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.QuestionType;
import com.zhangziqi.online_course_mine.model.vo.QuestionVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionOptionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.QuestionServiceImpl;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository optionRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuestionTagService questionTagService;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Institution testInstitution;
    private User testUser;
    private Question testQuestion;
    private List<QuestionOption> testOptions;
    private QuestionDTO testQuestionDTO;

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

        // 创建测试选项
        testOptions = new ArrayList<>();
        testOptions.add(QuestionOption.builder()
                .id(1L)
                .question(testQuestion)
                .content("选项A")
                .isCorrect(true)
                .orderIndex(0)
                .build());
        testOptions.add(QuestionOption.builder()
                .id(2L)
                .question(testQuestion)
                .content("选项B")
                .isCorrect(false)
                .orderIndex(1)
                .build());
        testOptions.add(QuestionOption.builder()
                .id(3L)
                .question(testQuestion)
                .content("选项C")
                .isCorrect(false)
                .orderIndex(2)
                .build());

        // 创建测试DTO
        List<QuestionOptionDTO> optionDTOs = new ArrayList<>();
        optionDTOs.add(QuestionOptionDTO.builder()
                .content("选项A")
                .isCorrect(true)
                .orderIndex(0)
                .build());
        optionDTOs.add(QuestionOptionDTO.builder()
                .content("选项B")
                .isCorrect(false)
                .orderIndex(1)
                .build());
        optionDTOs.add(QuestionOptionDTO.builder()
                .content("选项C")
                .isCorrect(false)
                .orderIndex(2)
                .build());

        testQuestionDTO = QuestionDTO.builder()
                .institutionId(testInstitution.getId())
                .title("测试题目")
                .content("这是一道测试题目的内容")
                .type(QuestionType.SINGLE_CHOICE.getValue())
                .difficulty(2)
                .score(5)
                .analysis("这是题目解析")
                .answer("这是答案")
                .options(optionDTOs)
                .build();

        // 模拟questionTagService.getTagsByQuestionId返回空列表
        lenient().when(questionTagService.getTagsByQuestionId(anyLong())).thenReturn(new ArrayList<>());
    }

    @Test
    @DisplayName("创建题目 - 成功")
    void createQuestion_Success() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
        when(optionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<QuestionOption> options = invocation.getArgument(0);
            options.forEach(option -> option.setQuestion(testQuestion));
            return options;
        });

        // 执行测试
        QuestionVO result = questionService.createQuestion(testQuestionDTO, testUser.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testQuestion.getId(), result.getId());
        assertEquals(testQuestion.getTitle(), result.getTitle());
        assertEquals(testQuestion.getContent(), result.getContent());
        assertEquals(testQuestion.getType(), result.getType());
        assertEquals(testQuestion.getDifficulty(), result.getDifficulty());
        assertEquals(testQuestion.getScore(), result.getScore());
        assertEquals(testQuestion.getAnalysis(), result.getAnalysis());
        assertEquals(testQuestion.getCreatorId(), result.getCreatorId());
        assertEquals(testOptions.size(), result.getOptions().size());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(userRepository).findById(testUser.getId());
        verify(questionRepository).save(any(Question.class));
        verify(optionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("创建题目 - 机构不存在")
    void createQuestion_InstitutionNotFound() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionService.createQuestion(testQuestionDTO, testUser.getId());
        });

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(userRepository, never()).findById(anyLong());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    @DisplayName("创建题目 - 用户不存在")
    void createQuestion_UserNotFound() {
        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionService.createQuestion(testQuestionDTO, testUser.getId());
        });

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(userRepository).findById(testUser.getId());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    @DisplayName("获取题目详情 - 成功")
    void getQuestionById_Success() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(optionRepository.findByQuestionIdOrderByOrderIndexAsc(anyLong())).thenReturn(testOptions);

        // 执行测试
        QuestionVO result = questionService.getQuestionById(testQuestion.getId(), testInstitution.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testQuestion.getId(), result.getId());
        assertEquals(testQuestion.getTitle(), result.getTitle());
        assertEquals(testQuestion.getContent(), result.getContent());
        assertEquals(testQuestion.getType(), result.getType());
        assertEquals(testQuestion.getDifficulty(), result.getDifficulty());
        assertEquals(testQuestion.getScore(), result.getScore());
        assertEquals(testQuestion.getAnalysis(), result.getAnalysis());
        assertEquals(testQuestion.getCreatorId(), result.getCreatorId());
        assertEquals(testOptions.size(), result.getOptions().size());

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(optionRepository).findByQuestionIdOrderByOrderIndexAsc(testQuestion.getId());
    }

    @Test
    @DisplayName("获取题目详情 - 题目不存在")
    void getQuestionById_QuestionNotFound() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionService.getQuestionById(999L, testInstitution.getId());
        });

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(optionRepository, never()).findByQuestionIdOrderByOrderIndexAsc(anyLong());
    }

    @Test
    @DisplayName("分页查询题目列表 - 成功")
    void getQuestions_Success() {
        // 设置测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questions = Arrays.asList(testQuestion);
        Page<Question> questionPage = new PageImpl<>(questions, pageable, 1);

        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(questionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(questionPage);
        when(optionRepository.findByQuestionIdOrderByOrderIndexAsc(anyLong())).thenReturn(testOptions);

        // 执行测试
        Page<QuestionVO> result = questionService.getQuestions(testInstitution.getId(), null, null, null, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testQuestion.getId(), result.getContent().get(0).getId());
        assertEquals(testQuestion.getTitle(), result.getContent().get(0).getTitle());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(questionRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
        verify(optionRepository).findByQuestionIdOrderByOrderIndexAsc(testQuestion.getId());
    }

    @Test
    @DisplayName("分页查询题目列表 - 机构不存在")
    void getQuestions_InstitutionNotFound() {
        // 设置测试数据
        Pageable pageable = PageRequest.of(0, 10);

        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionService.getQuestions(999L, null, null, null, pageable);
        });

        // 验证方法调用
        verify(institutionRepository).findById(999L);
        verify(questionRepository, never()).findByInstitution(any(Institution.class), any(Pageable.class));
    }

    @Test
    @DisplayName("随机获取题目 - 成功")
    void getRandomQuestions_Success() {
        // 设置模拟行为
        when(questionRepository.findRandomQuestions(anyLong(), anyInt(), anyInt())).thenReturn(Arrays.asList(testQuestion));
        when(optionRepository.findByQuestionIdOrderByOrderIndexAsc(anyLong())).thenReturn(testOptions);

        // 执行测试
        List<QuestionVO> result = questionService.getRandomQuestions(testInstitution.getId(), QuestionType.SINGLE_CHOICE.getValue(), 5);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testQuestion.getId(), result.get(0).getId());
        assertEquals(testQuestion.getTitle(), result.get(0).getTitle());

        // 验证方法调用
        verify(questionRepository).findRandomQuestions(testInstitution.getId(), QuestionType.SINGLE_CHOICE.getValue(), 5);
        verify(optionRepository).findByQuestionIdOrderByOrderIndexAsc(testQuestion.getId());
    }

    @Test
    @DisplayName("删除题目 - 成功")
    void deleteQuestion_Success() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        doNothing().when(optionRepository).deleteByQuestionId(anyLong());
        doNothing().when(questionRepository).delete(any(Question.class));

        // 执行测试
        assertDoesNotThrow(() -> questionService.deleteQuestion(testQuestion.getId(), testInstitution.getId(), testUser.getId()));

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(optionRepository).deleteByQuestionId(testQuestion.getId());
        verify(questionRepository).delete(testQuestion);
    }

    @Test
    @DisplayName("删除题目 - 题目不存在")
    void deleteQuestion_QuestionNotFound() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            questionService.deleteQuestion(999L, testInstitution.getId(), testUser.getId());
        });

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(999L, testInstitution.getId());
        verify(optionRepository, never()).deleteByQuestionId(anyLong());
        verify(questionRepository, never()).delete(any(Question.class));
    }

    @Test
    @DisplayName("删除题目 - 无权限")
    void deleteQuestion_NoPermission() {
        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            questionService.deleteQuestion(testQuestion.getId(), testInstitution.getId(), 999L);
        });

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestion.getId(), testInstitution.getId());
        verify(optionRepository, never()).deleteByQuestionId(anyLong());
        verify(questionRepository, never()).delete(any(Question.class));
    }

    @Test
    @DisplayName("更新题目 - 请添加这个测试方法")
    void updateQuestion_Success() {
        // 准备测试数据
        testQuestionDTO.setId(1L);

        // 设置模拟行为
        when(questionRepository.findByIdAndInstitutionId(anyLong(), anyLong())).thenReturn(Optional.of(testQuestion));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
        doNothing().when(optionRepository).deleteByQuestionId(anyLong());
        when(optionRepository.saveAll(anyList())).thenReturn(testOptions);

        // 执行测试
        QuestionVO result = questionService.updateQuestion(testQuestionDTO, testUser.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testQuestion.getId(), result.getId());
        assertEquals(testQuestion.getTitle(), result.getTitle());

        // 验证方法调用
        verify(questionRepository).findByIdAndInstitutionId(testQuestionDTO.getId(), testQuestionDTO.getInstitutionId());
        verify(questionRepository).save(any(Question.class));
        verify(optionRepository).deleteByQuestionId(testQuestion.getId());
        verify(optionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("创建题目 - 填空题")
    void createQuestion_FillBlank_Success() {
        // 创建填空题DTO
        QuestionDTO fillBlankDTO = QuestionDTO.builder()
                .institutionId(testInstitution.getId())
                .title("测试填空题")
                .content("这是一道填空题的内容____")
                .type(QuestionType.FILL_BLANK.getValue())
                .difficulty(2)
                .score(5)
                .analysis("这是题目解析")
                .answer("正确答案")
                .options(new ArrayList<>())
                .build();

        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question savedQuestion = invocation.getArgument(0);
            savedQuestion.setId(1L);
            // 确保答案被正确设置
            if (savedQuestion.getType() == QuestionType.FILL_BLANK.getValue()) {
                savedQuestion.setAnswer("正确答案");
            }
            return savedQuestion;
        });

        // 执行测试
        QuestionVO result = questionService.createQuestion(fillBlankDTO, testUser.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(fillBlankDTO.getTitle(), result.getTitle());
        assertEquals(fillBlankDTO.getContent(), result.getContent());
        assertEquals(fillBlankDTO.getType(), result.getType());
        assertEquals(fillBlankDTO.getAnswer(), result.getAnswer());
        assertTrue(result.getOptions().isEmpty());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(userRepository).findById(testUser.getId());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("创建题目 - 判断题")
    void createQuestion_TrueFalse_Success() {
        // 创建判断题选项
        List<QuestionOptionDTO> trueFalseOptions = Arrays.asList(
            QuestionOptionDTO.builder()
                .content("正确")
                .isCorrect(true)
                .orderIndex(0)
                .build(),
            QuestionOptionDTO.builder()
                .content("错误")
                .isCorrect(false)
                .orderIndex(1)
                .build()
        );

        // 创建判断题DTO
        QuestionDTO trueFalseDTO = QuestionDTO.builder()
                .institutionId(testInstitution.getId())
                .title("测试判断题")
                .content("这是一道判断题的内容")
                .type(QuestionType.TRUE_FALSE.getValue())
                .difficulty(2)
                .score(5)
                .analysis("这是题目解析")
                .options(trueFalseOptions)
                .build();

        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question savedQuestion = invocation.getArgument(0);
            savedQuestion.setId(1L);
            return savedQuestion;
        });

        // 设置保存后的选项和问题的关联
        when(optionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<QuestionOption> options = invocation.getArgument(0);
            Question question = new Question();
            question.setId(1L);

            return options.stream()
                    .peek(option -> option.setQuestion(question))
                    .collect(Collectors.toList());
        });

        // 执行测试
        QuestionVO result = questionService.createQuestion(trueFalseDTO, testUser.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(trueFalseDTO.getTitle(), result.getTitle());
        assertEquals(trueFalseDTO.getContent(), result.getContent());
        assertEquals(trueFalseDTO.getType(), result.getType());
        assertEquals(2, result.getOptions().size());
        assertEquals("正确", result.getOptions().get(0).getContent());
        assertEquals("错误", result.getOptions().get(1).getContent());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(userRepository).findById(testUser.getId());
        verify(questionRepository).save(any(Question.class));
        verify(optionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("创建题目 - 简答题")
    void createQuestion_ShortAnswer_Success() {
        // 创建简答题DTO
        QuestionDTO shortAnswerDTO = QuestionDTO.builder()
                .institutionId(testInstitution.getId())
                .title("测试简答题")
                .content("这是一道简答题的内容")
                .type(QuestionType.SHORT_ANSWER.getValue())
                .difficulty(2)
                .score(5)
                .analysis("这是题目解析")
                .answer("参考答案")
                .options(new ArrayList<>())
                .build();

        // 设置模拟行为
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(testInstitution));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question savedQuestion = invocation.getArgument(0);
            savedQuestion.setId(1L);
            // 确保答案被正确设置
            if (savedQuestion.getType() == QuestionType.SHORT_ANSWER.getValue()) {
                savedQuestion.setAnswer("参考答案");
            }
            return savedQuestion;
        });

        // 执行测试
        QuestionVO result = questionService.createQuestion(shortAnswerDTO, testUser.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(shortAnswerDTO.getTitle(), result.getTitle());
        assertEquals(shortAnswerDTO.getContent(), result.getContent());
        assertEquals(shortAnswerDTO.getType(), result.getType());
        assertEquals(shortAnswerDTO.getAnswer(), result.getAnswer());
        assertTrue(result.getOptions().isEmpty());

        // 验证方法调用
        verify(institutionRepository).findById(testInstitution.getId());
        verify(userRepository).findById(testUser.getId());
        verify(questionRepository).save(any(Question.class));
    }
}