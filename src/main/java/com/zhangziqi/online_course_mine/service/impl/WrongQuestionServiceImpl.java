package com.zhangziqi.online_course_mine.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.UserQuestionAnswerDTO;
import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.entity.UserWrongQuestion;
import com.zhangziqi.online_course_mine.model.enums.UserWrongQuestionStatus;
import com.zhangziqi.online_course_mine.model.vo.UserWrongQuestionVO;
import com.zhangziqi.online_course_mine.repository.ChapterRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.SectionRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.repository.UserWrongQuestionRepository;
import com.zhangziqi.online_course_mine.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 错题本服务接口实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {
    
    private final UserWrongQuestionRepository wrongQuestionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final SectionRepository sectionRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public UserWrongQuestion saveWrongQuestion(Long userId, Long courseId, Long sectionId, Long questionId, 
                                      UserQuestionAnswerDTO dto) {
        log.info("保存错题记录, 用户ID: {}, 课程ID: {}, 小节ID: {}, 题目ID: {}", 
                userId, courseId, sectionId, questionId);
        
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 查找课程
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
        
        // 查找小节
        Section section = null;
        if (sectionId != null) {
            section = sectionRepository.findById(sectionId)
                    .orElse(null);
        }
        
        // 查找章节
        Chapter chapter = null;
        if (section != null && section.getChapter() != null) {
            chapter = section.getChapter();
        }
        
        // 查找是否已存在该错题记录
        Optional<UserWrongQuestion> existingRecord = wrongQuestionRepository
                .findByUser_IdAndCourse_IdAndQuestionId(userId, courseId, questionId);
        
        UserWrongQuestion wrongQuestion;
        if (existingRecord.isPresent()) {
            // 更新现有记录
            wrongQuestion = existingRecord.get();
            // 错误计数增加
            Integer wrongCount = wrongQuestion.getStatus() != null ? wrongQuestion.getStatus() + 1 : 1;
            wrongQuestion.setStatus(UserWrongQuestionStatus.UNRESOLVED.getValue()); // 再次错误，标记为未解决
        } else {
            // 创建新记录
            wrongQuestion = UserWrongQuestion.builder()
                    .user(user)
                    .course(course)
                    .sectionId(sectionId)
                    .questionId(questionId)
                    .questionType(dto.getQuestionType())
                    .questionTitle(dto.getQuestionTitle())
                    .status(UserWrongQuestionStatus.UNRESOLVED.getValue())
                    .build();
        }
        
        // 序列化用户答案
        try {
            wrongQuestion.setUserAnswer(objectMapper.writeValueAsString(dto.getAnswers()));
            if (dto.getCorrectAnswers() != null) {
                wrongQuestion.setCorrectAnswers(objectMapper.writeValueAsString(dto.getCorrectAnswers()));
            }
        } catch (JsonProcessingException e) {
            log.error("序列化答案失败", e);
            throw new BusinessException(500, "保存答案失败");
        }
        
        // 保存错题记录
        UserWrongQuestion savedWrongQuestion = wrongQuestionRepository.save(wrongQuestion);
        
        log.info("成功保存错题记录, ID: {}", savedWrongQuestion.getId());
        return savedWrongQuestion;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserWrongQuestionVO> getUserWrongQuestions(Long userId, Pageable pageable) {
        log.info("分页获取用户错题, 用户ID: {}, 页码: {}, 每页数量: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<UserWrongQuestion> wrongQuestionsPage = wrongQuestionRepository.findByUser_Id(userId, pageable);
        
        return wrongQuestionsPage.map(this::convertToVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWrongQuestionVO> getUserWrongQuestions(Long userId) {
        log.info("获取用户所有错题, 用户ID: {}", userId);
        
        List<UserWrongQuestion> wrongQuestions = wrongQuestionRepository.findByUser_Id(userId);
        
        return convertToVOList(wrongQuestions);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserWrongQuestionVO> getUserCourseWrongQuestions(Long userId, Long courseId, Pageable pageable) {
        log.info("分页获取用户课程错题, 用户ID: {}, 课程ID: {}, 页码: {}, 每页数量: {}", 
                userId, courseId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<UserWrongQuestion> wrongQuestionsPage = 
                wrongQuestionRepository.findByUser_IdAndCourse_Id(userId, courseId, pageable);
        
        return wrongQuestionsPage.map(this::convertToVO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserWrongQuestionVO> getUserCourseWrongQuestions(Long userId, Long courseId) {
        log.info("获取用户课程所有错题, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        List<UserWrongQuestion> wrongQuestions = wrongQuestionRepository.findByUser_IdAndCourse_Id(userId, courseId);
        
        return convertToVOList(wrongQuestions);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserWrongQuestionVO> getUserUnresolvedWrongQuestions(Long userId, Pageable pageable) {
        log.info("分页获取用户未解决错题, 用户ID: {}, 页码: {}, 每页数量: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        // 获取状态为未解决的错题
        Page<UserWrongQuestion> wrongQuestionsPage = wrongQuestionRepository.findByUser_IdAndStatus(
                userId, UserWrongQuestionStatus.UNRESOLVED.getValue(), pageable);
        
        return wrongQuestionsPage.map(this::convertToVO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserWrongQuestionVO> getUserUnresolvedWrongQuestions(Long userId) {
        log.info("获取用户所有未解决错题, 用户ID: {}", userId);
        
        // 获取状态为未解决的错题
        List<UserWrongQuestion> wrongQuestions = wrongQuestionRepository.findByUser_IdAndStatus(
                userId, UserWrongQuestionStatus.UNRESOLVED.getValue());
        
        return convertToVOList(wrongQuestions);
    }
    
    @Override
    @Transactional
    public void resolveWrongQuestion(Long userId, Long wrongQuestionId) {
        log.info("标记错题为已解决, 用户ID: {}, 错题ID: {}", userId, wrongQuestionId);
        
        UserWrongQuestion wrongQuestion = wrongQuestionRepository.findById(wrongQuestionId)
                .orElseThrow(() -> new ResourceNotFoundException("错题记录不存在"));
        
        // 验证所有权
        if (!wrongQuestion.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作此错题记录");
        }
        
        wrongQuestion.setStatus(UserWrongQuestionStatus.RESOLVED.getValue());
        wrongQuestionRepository.save(wrongQuestion);
        
        log.info("成功标记错题为已解决");
    }
    
    @Override
    @Transactional
    public void deleteWrongQuestion(Long userId, Long wrongQuestionId) {
        log.info("删除错题, 用户ID: {}, 错题ID: {}", userId, wrongQuestionId);
        
        UserWrongQuestion wrongQuestion = wrongQuestionRepository.findById(wrongQuestionId)
                .orElseThrow(() -> new ResourceNotFoundException("错题记录不存在"));
        
        // 验证所有权
        if (!wrongQuestion.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作此错题记录");
        }
        
        wrongQuestionRepository.delete(wrongQuestion);
        
        log.info("成功删除错题");
    }
    
    @Override
    @Transactional
    public void deleteAllUserWrongQuestions(Long userId) {
        log.info("删除用户所有错题, 用户ID: {}", userId);
        
        wrongQuestionRepository.deleteByUser_Id(userId);
        
        log.info("成功删除用户所有错题");
    }
    
    @Override
    @Transactional
    public void deleteAllUserWrongQuestionsByCourse(Long userId, Long courseId) {
        log.info("删除用户课程所有错题, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        wrongQuestionRepository.deleteByUser_IdAndCourse_Id(userId, courseId);
        
        log.info("成功删除用户课程所有错题");
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countUserWrongQuestions(Long userId) {
        return wrongQuestionRepository.countByUser_Id(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countUserUnresolvedWrongQuestions(Long userId) {
        return wrongQuestionRepository.countByUser_IdAndStatus(userId, UserWrongQuestionStatus.UNRESOLVED.getValue());
    }
    
    /**
     * 将错题实体转换为VO
     */
    private UserWrongQuestionVO convertToVO(UserWrongQuestion entity) {
        if (entity == null) {
            return null;
        }
        
        List<String> userAnswers = new ArrayList<>();
        List<String> correctAnswers = new ArrayList<>();
        
        try {
            if (entity.getUserAnswer() != null) {
                userAnswers = objectMapper.readValue(entity.getUserAnswer(), List.class);
            }
            if (entity.getCorrectAnswers() != null) {
                correctAnswers = objectMapper.readValue(entity.getCorrectAnswers(), List.class);
            }
        } catch (JsonProcessingException e) {
            log.error("解析答案JSON失败", e);
        }
        
        return UserWrongQuestionVO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .courseId(entity.getCourse().getId())
                .courseTitle(entity.getCourse().getTitle())
                .sectionId(entity.getSectionId())
                .questionId(entity.getQuestionId())
                .questionTitle(entity.getQuestionTitle())
                .questionType(entity.getQuestionType())
                .userAnswers(userAnswers)
                .correctAnswers(correctAnswers)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 将错题实体列表转换为VO列表
     */
    private List<UserWrongQuestionVO> convertToVOList(List<UserWrongQuestion> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
} 