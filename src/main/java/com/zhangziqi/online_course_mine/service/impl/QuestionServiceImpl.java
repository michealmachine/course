package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.QuestionDTO;
import com.zhangziqi.online_course_mine.model.dto.QuestionOptionDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Question;
import com.zhangziqi.online_course_mine.model.entity.QuestionOption;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.QuestionType;
import com.zhangziqi.online_course_mine.model.vo.QuestionOptionVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionOptionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;

    /**
     * 创建题目
     */
    @Override
    @Transactional
    public QuestionVO createQuestion(QuestionDTO questionDTO, Long creatorId) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(questionDTO.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));
        
        // 验证创建者是否存在
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 验证题目类型
        validateQuestionType(questionDTO.getType());
        
        // 验证选项
        validateOptions(questionDTO);
        
        try {
            // 创建题目实体
            Question question = Question.builder()
                    .institution(institution)
                    .title(questionDTO.getTitle())
                    .content(questionDTO.getContent())
                    .type(questionDTO.getType())
                    .difficulty(questionDTO.getDifficulty())
                    .score(questionDTO.getScore())
                    .analysis(questionDTO.getAnalysis())
                    .creatorId(creatorId)
                    .creatorName(creator.getName())
                    .build();
            
            // 保存题目
            Question savedQuestion = questionRepository.save(question);
            
            // 保存选项
            List<QuestionOption> options = saveOptions(questionDTO.getOptions(), savedQuestion);
            
            // 构建响应对象
            return buildQuestionVO(savedQuestion, options);
        } catch (DataIntegrityViolationException e) {
            log.error("创建题目失败", e);
            throw new BusinessException("创建题目失败：数据完整性异常");
        } catch (Exception e) {
            log.error("创建题目失败", e);
            throw new BusinessException("创建题目失败：" + e.getMessage());
        }
    }

    /**
     * 更新题目
     */
    @Override
    @Transactional
    public QuestionVO updateQuestion(QuestionDTO questionDTO, Long userId) {
        // 验证题目是否存在
        Question existingQuestion = questionRepository.findByIdAndInstitutionId(
                        questionDTO.getId(), questionDTO.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        
        // 验证权限（只有题目创建者或管理员可以修改）
        if (!existingQuestion.getCreatorId().equals(userId)) {
            // 此处可增加管理员权限检查
            throw new BusinessException("无权限修改此题目");
        }
        
        // 验证题目类型
        validateQuestionType(questionDTO.getType());
        
        // 验证选项
        validateOptions(questionDTO);
        
        try {
            // 更新题目属性
            existingQuestion.setTitle(questionDTO.getTitle());
            existingQuestion.setContent(questionDTO.getContent());
            existingQuestion.setType(questionDTO.getType());
            existingQuestion.setDifficulty(questionDTO.getDifficulty());
            existingQuestion.setScore(questionDTO.getScore());
            existingQuestion.setAnalysis(questionDTO.getAnalysis());
            
            // 保存更新的题目
            Question updatedQuestion = questionRepository.save(existingQuestion);
            
            // 删除现有选项
            optionRepository.deleteByQuestionId(updatedQuestion.getId());
            
            // 保存新选项
            List<QuestionOption> options = saveOptions(questionDTO.getOptions(), updatedQuestion);
            
            // 构建响应对象
            return buildQuestionVO(updatedQuestion, options);
        } catch (Exception e) {
            log.error("更新题目失败", e);
            throw new BusinessException("更新题目失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询题目详情
     */
    @Override
    public QuestionVO getQuestionById(Long questionId, Long institutionId) {
        // 获取题目
        Question question = questionRepository.findByIdAndInstitutionId(questionId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        
        // 获取题目选项
        List<QuestionOption> options = optionRepository.findByQuestionIdOrderByOrderIndexAsc(questionId);
        
        // 构建响应对象
        return buildQuestionVO(question, options);
    }

    /**
     * 删除题目
     */
    @Override
    @Transactional
    public void deleteQuestion(Long questionId, Long institutionId, Long userId) {
        // 获取题目
        Question question = questionRepository.findByIdAndInstitutionId(questionId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        
        // 验证权限（只有题目创建者或管理员可以删除）
        if (!question.getCreatorId().equals(userId)) {
            // 此处可增加管理员权限检查
            throw new BusinessException("无权限删除此题目");
        }
        
        try {
            // 删除题目选项
            optionRepository.deleteByQuestionId(questionId);
            
            // 删除题目
            questionRepository.delete(question);
        } catch (Exception e) {
            log.error("删除题目失败", e);
            throw new BusinessException("删除题目失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询题目列表
     */
    @Override
    public Page<QuestionVO> getQuestions(Long institutionId, Integer type, Integer difficulty, String keyword, Pageable pageable) {
        // 获取机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));
        
        Page<Question> questionPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 根据关键词搜索
            questionPage = questionRepository.searchByKeyword(institution, keyword.trim(), pageable);
        } else if (type != null) {
            // 根据类型过滤
            questionPage = questionRepository.findByInstitutionAndType(institution, type, pageable);
        } else if (difficulty != null) {
            // 根据难度过滤
            questionPage = questionRepository.findByInstitutionAndDifficulty(institution, difficulty, pageable);
        } else {
            // 查询所有题目
            questionPage = questionRepository.findByInstitution(institution, pageable);
        }
        
        // 转换为VO对象
        return questionPage.map(question -> {
            List<QuestionOption> options = optionRepository.findByQuestionIdOrderByOrderIndexAsc(question.getId());
            return buildQuestionVO(question, options);
        });
    }

    /**
     * 随机获取指定数量的题目
     */
    @Override
    public List<QuestionVO> getRandomQuestions(Long institutionId, Integer type, int count) {
        // 获取随机题目
        List<Question> questions = questionRepository.findRandomQuestions(institutionId, type, count);
        
        // 转换为VO对象
        return questions.stream()
                .map(question -> {
                    List<QuestionOption> options = optionRepository.findByQuestionIdOrderByOrderIndexAsc(question.getId());
                    return buildQuestionVO(question, options);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据ID列表批量获取题目
     */
    @Override
    public List<QuestionVO> getQuestionsByIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return List.of();
        }
        
        // 查询题目
        List<Question> questions = questionRepository.findAllById(questionIds);
        
        // 转换为VO对象
        return questions.stream()
                .map(question -> {
                    List<QuestionOption> options = optionRepository.findByQuestionIdOrderByOrderIndexAsc(question.getId());
                    return buildQuestionVO(question, options);
                })
                .collect(Collectors.toList());
    }

    /**
     * 保存题目选项
     */
    private List<QuestionOption> saveOptions(List<QuestionOptionDTO> optionDTOs, Question question) {
        List<QuestionOption> options = optionDTOs.stream()
                .map(dto -> QuestionOption.builder()
                        .question(question)
                        .content(dto.getContent())
                        .isCorrect(dto.getIsCorrect())
                        .orderIndex(dto.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
        
        return optionRepository.saveAll(options);
    }

    /**
     * 验证题目类型
     */
    private void validateQuestionType(Integer type) {
        try {
            QuestionType.getByValue(type);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的题目类型");
        }
    }

    /**
     * 验证选项
     */
    private void validateOptions(QuestionDTO questionDTO) {
        // 至少有两个选项
        if (questionDTO.getOptions() == null || questionDTO.getOptions().size() < 2) {
            throw new BusinessException("题目至少需要两个选项");
        }
        
        // 验证正确选项的数量
        long correctOptionsCount = questionDTO.getOptions().stream()
                .filter(QuestionOptionDTO::getIsCorrect)
                .count();
        
        if (correctOptionsCount == 0) {
            throw new BusinessException("至少需要一个正确选项");
        }
        
        // 单选题只能有一个正确选项
        if (questionDTO.getType().equals(QuestionType.SINGLE_CHOICE.getValue()) && correctOptionsCount > 1) {
            throw new BusinessException("单选题只能有一个正确选项");
        }
        
        // 多选题至少需要两个正确选项
        if (questionDTO.getType().equals(QuestionType.MULTIPLE_CHOICE.getValue()) && correctOptionsCount < 2) {
            throw new BusinessException("多选题至少需要两个正确选项");
        }
    }

    /**
     * 构建题目视图对象
     */
    private QuestionVO buildQuestionVO(Question question, List<QuestionOption> options) {
        // 获取题目类型描述
        String typeDesc = QuestionType.getByValue(question.getType()).getDescription();
        
        // 获取难度描述
        String difficultyDesc;
        switch (question.getDifficulty()) {
            case 1:
                difficultyDesc = "简单";
                break;
            case 2:
                difficultyDesc = "中等";
                break;
            case 3:
                difficultyDesc = "困难";
                break;
            default:
                difficultyDesc = "未知";
        }
        
        // 构建选项VO
        List<QuestionOptionVO> optionVOs = options.stream()
                .map(option -> QuestionOptionVO.builder()
                        .id(option.getId())
                        .questionId(question.getId())
                        .content(option.getContent())
                        .isCorrect(option.getIsCorrect())
                        .orderIndex(option.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
        
        // 构建题目VO
        return QuestionVO.builder()
                .id(question.getId())
                .institutionId(question.getInstitutionId())
                .title(question.getTitle())
                .content(question.getContent())
                .type(question.getType())
                .typeDesc(typeDesc)
                .difficulty(question.getDifficulty())
                .difficultyDesc(difficultyDesc)
                .score(question.getScore())
                .analysis(question.getAnalysis())
                .options(optionVOs)
                .creatorId(question.getCreatorId())
                .creatorName(question.getCreatorName())
                .createdTime(question.getCreatedTime())
                .updatedTime(question.getUpdatedTime())
                .build();
    }
} 