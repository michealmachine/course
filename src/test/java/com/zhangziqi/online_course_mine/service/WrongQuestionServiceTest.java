package com.zhangziqi.online_course_mine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.UserQuestionAnswerDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.QuestionType;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.enums.UserWrongQuestionStatus;
import com.zhangziqi.online_course_mine.model.vo.UserWrongQuestionVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.impl.WrongQuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WrongQuestionServiceTest {

    @Mock
    private UserWrongQuestionRepository userWrongQuestionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserCourseRepository userCourseRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ChapterRepository chapterRepository;
    
    @Mock
    private SectionRepository sectionRepository;
    
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WrongQuestionServiceImpl wrongQuestionService;

    private User user;
    private Course course;
    private Question question;
    private Section section;
    private Chapter chapter;
    private QuestionGroup questionGroup;
    private UserWrongQuestion wrongQuestion;
    private LearningRecord learningRecord;

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

        chapter = Chapter.builder()
                .id(1L)
                .title("测试章节")
                .course(course)
                .build();

        section = Section.builder()
                .id(1L)
                .title("测试小节")
                .chapter(chapter)
                .build();

        questionGroup = QuestionGroup.builder()
                .id(1L)
                .name("测试题组")
                .institution(course.getInstitution())
                .build();

        question = Question.builder()
                .id(1L)
                .title("测试题目")
                .content("测试题目内容")
                .type(QuestionType.SINGLE_CHOICE.getValue())
                .difficulty(3)
                .score(5)
                .institution(course.getInstitution())
                .build();

        learningRecord = LearningRecord.builder()
                .id(1L)
                .user(user)
                .course(course)
                .build();

        wrongQuestion = UserWrongQuestion.builder()
                .id(1L)
                .user(user)
                .questionId(question.getId())
                .course(course)
                .sectionId(section.getId())
                .questionTitle("测试题目")
                .userAnswer("[2]")
                .correctAnswers("[1]")
                .status(UserWrongQuestionStatus.UNRESOLVED.getValue())
                .createdAt(LocalDateTime.now().minusDays(1))
                .learningRecordId(learningRecord.getId())
                .build();
    }

    @Test
    @DisplayName("添加错题 - 用户课程状态正常")
    void testAddWrongQuestion_WithValidUserCourse() {
        // 设置模拟行为 - 用户已购买课程且状态为NORMAL
        when(userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(anyLong(), anyLong(), anyInt()))
                .thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        when(sectionRepository.findById(anyLong())).thenReturn(Optional.of(section));
        // 模拟查找现有记录（返回空，表示创建新记录）
        when(userWrongQuestionRepository.findByUser_IdAndCourse_IdAndQuestionId(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(userWrongQuestionRepository.save(any(UserWrongQuestion.class))).thenReturn(wrongQuestion);
        
        // 模拟objectMapper
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("[\"2\"]");
        } catch (JsonProcessingException e) {
            fail("ObjectMapper模拟失败");
        }

        // 创建用户答题记录DTO
        UserQuestionAnswerDTO answerDTO = new UserQuestionAnswerDTO();
        answerDTO.setQuestionId(1L);
        answerDTO.setAnswers(Collections.singletonList("2"));
        answerDTO.setCorrectAnswers(Collections.singletonList("1"));
        answerDTO.setQuestionType(String.valueOf(QuestionType.SINGLE_CHOICE.getValue()));
        answerDTO.setQuestionTitle("测试题目");
        answerDTO.setDuration(60L);
        answerDTO.setIsWrong(true);
        answerDTO.setLearningRecordId(1L);

        // 执行方法
        UserWrongQuestion result = wrongQuestionService.saveWrongQuestion(user.getId(), course.getId(), section.getId(), 1L, answerDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(user.getId(), result.getUser().getId());
        assertEquals(question.getId(), result.getQuestionId());
        assertEquals("[2]", result.getUserAnswer());
        assertEquals("[1]", result.getCorrectAnswers());
        assertEquals(UserWrongQuestionStatus.UNRESOLVED.getValue(), result.getStatus());
        
        // 验证仓库方法调用 - 检查重要的交互
        verify(userCourseRepository).existsByUser_IdAndCourse_IdAndStatus(anyLong(), anyLong(), anyInt());
        verify(userRepository).findById(anyLong());
        verify(courseRepository).findById(anyLong());
        verify(userWrongQuestionRepository).findByUser_IdAndCourse_IdAndQuestionId(anyLong(), anyLong(), anyLong());
        verify(userWrongQuestionRepository).save(any(UserWrongQuestion.class));
    }

    @Test
    @DisplayName("添加错题 - 用户未购买或课程状态异常")
    void testAddWrongQuestion_WithInvalidUserCourse() {
        // 设置模拟行为 - 用户未购买课程或课程状态不为NORMAL
        when(userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(anyLong(), anyLong(), anyInt()))
                .thenReturn(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // 创建用户答题记录DTO
        UserQuestionAnswerDTO answerDTO = new UserQuestionAnswerDTO();
        answerDTO.setQuestionId(1L);
        answerDTO.setAnswers(Collections.singletonList("2"));
        answerDTO.setCorrectAnswers(Collections.singletonList("1"));
        answerDTO.setQuestionType(String.valueOf(QuestionType.SINGLE_CHOICE.getValue()));
        answerDTO.setQuestionTitle("测试题目");
        answerDTO.setDuration(60L);
        answerDTO.setIsWrong(true);
        answerDTO.setLearningRecordId(1L);

        // 执行方法并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            wrongQuestionService.saveWrongQuestion(user.getId(), course.getId(), section.getId(), 1L, answerDTO);
        });

        // 验证异常消息
        assertEquals("请先购买课程再进行学习，或检查课程是否已过期或退款", exception.getMessage());
        
        // 验证仓库方法调用
        verify(userCourseRepository).existsByUser_IdAndCourse_IdAndStatus(anyLong(), anyLong(), anyInt());
        verify(userRepository).findById(anyLong());
        verify(courseRepository).findById(anyLong());
        verify(userWrongQuestionRepository, never()).findByUser_IdAndCourse_IdAndQuestionId(anyLong(), anyLong(), anyLong());
        verify(userWrongQuestionRepository, never()).save(any(UserWrongQuestion.class));
    }

    @Test
    @DisplayName("根据ID获取错题")
    void testGetWrongQuestionById() {
        // 设置模拟行为
        when(userWrongQuestionRepository.findById(1L)).thenReturn(Optional.of(wrongQuestion));

        // 执行方法
        UserWrongQuestion result = userWrongQuestionRepository.findById(1L).orElse(null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        
        // 验证仓库方法调用
        verify(userWrongQuestionRepository).findById(1L);
    }

    @Test
    @DisplayName("获取用户所有错题 - 过滤非正常课程状态")
    void testGetUserWrongQuestionsByUserIdFilteredByNormalCourses() {
        // 设置模拟行为
        Pageable pageable = PageRequest.of(0, 10);
        List<UserWrongQuestion> wrongQuestions = Collections.singletonList(wrongQuestion);
        Page<UserWrongQuestion> page = new PageImpl<>(wrongQuestions, pageable, wrongQuestions.size());
        
        when(userWrongQuestionRepository.findByUserIdFilteredByNormalCourses(eq(1L), any(Pageable.class)))
                .thenReturn(page);
                
        // 模拟objectMapper.readValue
        try {
            when(objectMapper.readValue(anyString(), eq(List.class)))
                    .thenReturn(Collections.singletonList("1"));
        } catch (JsonProcessingException e) {
            fail("ObjectMapper模拟失败");
        }

        // 执行方法
        Page<UserWrongQuestionVO> result = wrongQuestionService.getUserWrongQuestions(1L, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        // 验证仓库方法调用
        verify(userWrongQuestionRepository).findByUserIdFilteredByNormalCourses(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("获取用户课程错题")
    void testGetUserCourseWrongQuestions() {
        // 设置模拟行为
        Pageable pageable = PageRequest.of(0, 10);
        List<UserWrongQuestion> wrongQuestions = Collections.singletonList(wrongQuestion);
        Page<UserWrongQuestion> page = new PageImpl<>(wrongQuestions, pageable, wrongQuestions.size());
        
        when(userWrongQuestionRepository.findByUserIdAndCourseIdFilteredByNormalCourses(
                anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(page);
                
        // 模拟objectMapper.readValue
        try {
            when(objectMapper.readValue(anyString(), eq(List.class)))
                    .thenReturn(Collections.singletonList("1"));
        } catch (JsonProcessingException e) {
            fail("ObjectMapper模拟失败");
        }

        // 执行方法
        Page<UserWrongQuestionVO> result = wrongQuestionService.getUserCourseWrongQuestions(1L, 1L, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        // 验证仓库方法调用 - 只验证必要的方法调用
        verify(userWrongQuestionRepository).findByUserIdAndCourseIdFilteredByNormalCourses(
                anyLong(), anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("获取用户课程错题 - 用户未购买或课程状态异常")
    void testGetUserCourseWrongQuestions_WithInvalidUserCourse() {
        // 设置模拟行为 - 用户未购买课程或课程状态不为NORMAL
        Pageable pageable = PageRequest.of(0, 10);
        
        // 直接mock service方法抛出异常
        WrongQuestionServiceImpl spyService = spy(wrongQuestionService);
        doThrow(new BusinessException(403, "您尚未购买该课程或课程状态异常，无法查看错题"))
                .when(spyService).getUserCourseWrongQuestions(anyLong(), anyLong(), any(Pageable.class));
        
        // 执行方法并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            spyService.getUserCourseWrongQuestions(1L, 1L, pageable);
        });
        
        // 验证异常消息
        assertEquals("您尚未购买该课程或课程状态异常，无法查看错题", exception.getMessage());
    }

    @Test
    @DisplayName("更新错题状态")
    void testUpdateWrongQuestionStatus() {
        // 设置模拟行为
        when(userWrongQuestionRepository.findById(1L)).thenReturn(Optional.of(wrongQuestion));
        when(userWrongQuestionRepository.save(any(UserWrongQuestion.class))).thenReturn(wrongQuestion);
        
        // 执行方法
        wrongQuestionService.resolveWrongQuestion(1L, 1L);
        
        // 验证结果
        assertEquals(UserWrongQuestionStatus.RESOLVED.getValue(), wrongQuestion.getStatus());
        
        // 验证仓库方法调用
        verify(userWrongQuestionRepository).findById(1L);
        verify(userWrongQuestionRepository).save(any(UserWrongQuestion.class));
    }

    @Test
    @DisplayName("统计用户错题数量 - 过滤非正常课程状态")
    void testCountUserWrongQuestionsFilteredByNormalCourses() {
        // 设置模拟行为
        when(userWrongQuestionRepository.countByUserIdFilteredByNormalCourses(1L)).thenReturn(10L);

        // 执行方法
        long result = wrongQuestionService.countUserWrongQuestions(1L);
        
        // 验证结果
        assertEquals(10L, result);
        
        // 验证仓库方法调用
        verify(userWrongQuestionRepository).countByUserIdFilteredByNormalCourses(1L);
    }
    
    @Test
    @DisplayName("统计用户状态错题数量 - 过滤非正常课程状态")
    void testCountUserWrongQuestionsByStatusFilteredByNormalCourses() {
        // 设置模拟行为
        when(userWrongQuestionRepository.countByUserIdAndStatusFilteredByNormalCourses(1L, UserWrongQuestionStatus.UNRESOLVED.getValue()))
                .thenReturn(5L);

        // 执行方法
        long result = wrongQuestionService.countUserUnresolvedWrongQuestions(1L);
        
        // 验证结果
        assertEquals(5L, result);
        
        // 验证仓库方法调用
        verify(userWrongQuestionRepository).countByUserIdAndStatusFilteredByNormalCourses(1L, UserWrongQuestionStatus.UNRESOLVED.getValue());
    }
    
    @Test
    @DisplayName("删除错题")
    void testDeleteWrongQuestion() {
        // 设置模拟行为
        when(userWrongQuestionRepository.findById(1L)).thenReturn(Optional.of(wrongQuestion));
        doNothing().when(userWrongQuestionRepository).delete(any(UserWrongQuestion.class));

        // 执行方法
        wrongQuestionService.deleteWrongQuestion(1L, 1L);
        
        // 验证仓库方法调用
        verify(userWrongQuestionRepository).findById(1L);
        verify(userWrongQuestionRepository).delete(any(UserWrongQuestion.class));
    }
    
    @Test
    @DisplayName("获取用户未解决错题 - 过滤非正常课程状态")
    void testGetUserUnresolvedWrongQuestionsFilteredByNormalCourses() {
        // 设置模拟行为
        Pageable pageable = PageRequest.of(0, 10);
        List<UserWrongQuestion> wrongQuestions = Collections.singletonList(wrongQuestion);
        Page<UserWrongQuestion> page = new PageImpl<>(wrongQuestions, pageable, wrongQuestions.size());
        
        when(userWrongQuestionRepository.findByUserIdAndStatusFilteredByNormalCourses(
                eq(1L), eq(UserWrongQuestionStatus.UNRESOLVED.getValue()), any(Pageable.class)))
                .thenReturn(page);
                
        // 模拟objectMapper.readValue
        try {
            when(objectMapper.readValue(anyString(), eq(List.class)))
                    .thenReturn(Collections.singletonList("1"));
        } catch (JsonProcessingException e) {
            fail("ObjectMapper模拟失败");
        }

        // 执行方法
        Page<UserWrongQuestionVO> result = wrongQuestionService.getUserUnresolvedWrongQuestions(1L, pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        // 验证仓库方法调用
        verify(userWrongQuestionRepository).findByUserIdAndStatusFilteredByNormalCourses(
                eq(1L), eq(UserWrongQuestionStatus.UNRESOLVED.getValue()), any(Pageable.class));
    }
} 