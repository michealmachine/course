package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Question;
import com.zhangziqi.online_course_mine.model.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目选项Repository
 */
@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    
    /**
     * 根据题目查询所有选项，按照顺序排序
     */
    List<QuestionOption> findByQuestionOrderByOrderIndexAsc(Question question);
    
    /**
     * 根据题目ID查询所有选项，按照顺序排序
     */
    @Query("SELECT qo FROM QuestionOption qo WHERE qo.question.id = :questionId ORDER BY qo.orderIndex ASC")
    List<QuestionOption> findByQuestionIdOrderByOrderIndexAsc(@Param("questionId") Long questionId);
    
    /**
     * 删除题目的所有选项
     */
    @Modifying
    @Query("DELETE FROM QuestionOption qo WHERE qo.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 获取题目的正确选项
     */
    @Query("SELECT qo FROM QuestionOption qo WHERE qo.question.id = :questionId AND qo.isCorrect = true")
    List<QuestionOption> findCorrectOptionsByQuestionId(@Param("questionId") Long questionId);
    
    /**
     * 统计题目选项数量
     */
    long countByQuestionId(Long questionId);
} 