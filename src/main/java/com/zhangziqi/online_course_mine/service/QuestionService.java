package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.QuestionDTO;
import com.zhangziqi.online_course_mine.model.vo.QuestionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 题目服务接口
 */
public interface QuestionService {
    
    /**
     * 创建题目
     *
     * @param questionDTO 题目数据
     * @param creatorId 创建者ID
     * @return 创建后的题目信息
     */
    QuestionVO createQuestion(QuestionDTO questionDTO, Long creatorId);
    
    /**
     * 更新题目
     *
     * @param questionDTO 题目更新数据
     * @param userId 当前用户ID
     * @return 更新后的题目信息
     */
    QuestionVO updateQuestion(QuestionDTO questionDTO, Long userId);
    
    /**
     * 根据ID查询题目详情
     *
     * @param questionId 题目ID
     * @param institutionId 机构ID
     * @return 题目详情
     */
    QuestionVO getQuestionById(Long questionId, Long institutionId);
    
    /**
     * 删除题目
     *
     * @param questionId 题目ID
     * @param institutionId 机构ID
     * @param userId 用户ID
     */
    void deleteQuestion(Long questionId, Long institutionId, Long userId);
    
    /**
     * 分页查询题目列表
     *
     * @param institutionId 机构ID
     * @param type 题目类型（可选）
     * @param difficulty 难度级别（可选）
     * @param keyword 关键词（可选）
     * @param pageable 分页参数
     * @return 分页题目列表
     */
    Page<QuestionVO> getQuestions(Long institutionId, Integer type, Integer difficulty, String keyword, Pageable pageable);
    
    /**
     * 随机获取指定数量的题目
     *
     * @param institutionId 机构ID
     * @param type 题目类型
     * @param count 题目数量
     * @return 题目列表
     */
    List<QuestionVO> getRandomQuestions(Long institutionId, Integer type, int count);
    
    /**
     * 根据ID列表批量获取题目
     *
     * @param questionIds 题目ID列表
     * @return 题目列表
     */
    List<QuestionVO> getQuestionsByIds(List<Long> questionIds);
} 