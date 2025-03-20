package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.UserQuestionAnswerDTO;
import com.zhangziqi.online_course_mine.model.entity.UserWrongQuestion;
import com.zhangziqi.online_course_mine.model.vo.UserWrongQuestionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 错题本服务接口
 */
public interface WrongQuestionService {
    
    /**
     * 保存错题记录
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param sectionId 小节ID
     * @param questionId 问题ID
     * @param answer 用户回答内容
     * @return 错题记录
     */
    UserWrongQuestion saveWrongQuestion(Long userId, Long courseId, Long sectionId, Long questionId, 
                                        UserQuestionAnswerDTO answer);
    
    /**
     * 将错题标记为已解决
     * @param userId 用户ID
     * @param wrongQuestionId 错题记录ID
     */
    void resolveWrongQuestion(Long userId, Long wrongQuestionId);
    
    /**
     * 删除错题记录
     * @param userId 用户ID
     * @param wrongQuestionId 错题记录ID
     */
    void deleteWrongQuestion(Long userId, Long wrongQuestionId);
    
    /**
     * 获取用户的所有错题（分页）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 错题分页列表
     */
    Page<UserWrongQuestionVO> getUserWrongQuestions(Long userId, Pageable pageable);
    
    /**
     * 获取用户的所有错题（不分页，谨慎使用）
     * @param userId 用户ID
     * @return 错题列表
     */
    List<UserWrongQuestionVO> getUserWrongQuestions(Long userId);
    
    /**
     * 获取用户特定课程的错题（分页）
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 错题分页列表
     */
    Page<UserWrongQuestionVO> getUserCourseWrongQuestions(Long userId, Long courseId, Pageable pageable);
    
    /**
     * 获取用户特定课程的错题（不分页，谨慎使用）
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 错题列表
     */
    List<UserWrongQuestionVO> getUserCourseWrongQuestions(Long userId, Long courseId);
    
    /**
     * 获取用户未解决的错题（分页）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 错题分页列表
     */
    Page<UserWrongQuestionVO> getUserUnresolvedWrongQuestions(Long userId, Pageable pageable);
    
    /**
     * 获取用户未解决的错题（不分页，谨慎使用）
     * @param userId 用户ID
     * @return 错题列表
     */
    List<UserWrongQuestionVO> getUserUnresolvedWrongQuestions(Long userId);

    /**
     * 删除用户所有错题
     * @param userId 用户ID
     */
    @Transactional
    void deleteAllUserWrongQuestions(Long userId);

    /**
     * 删除用户特定课程的所有错题
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    @Transactional
    void deleteAllUserWrongQuestionsByCourse(Long userId, Long courseId);

    /**
     * 统计用户的错题总数
     * @param userId 用户ID
     * @return 错题总数
     */
    long countUserWrongQuestions(Long userId);
    
    /**
     * 统计用户未解决的错题数量
     * @param userId 用户ID
     * @return 未解决的错题数量
     */
    long countUserUnresolvedWrongQuestions(Long userId);
} 