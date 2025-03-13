package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Question;
import com.zhangziqi.online_course_mine.model.entity.QuestionTag;
import com.zhangziqi.online_course_mine.model.entity.QuestionTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 题目标签映射数据访问层
 */
@Repository
public interface QuestionTagMappingRepository extends JpaRepository<QuestionTagMapping, Long> {

    /**
     * 根据题目和标签查询映射
     */
    Optional<QuestionTagMapping> findByQuestionAndTag(Question question, QuestionTag tag);

    /**
     * 删除题目和标签的映射
     */
    @Modifying
    @Query("DELETE FROM QuestionTagMapping m WHERE m.question.id = :questionId AND m.tag.id = :tagId")
    void deleteByQuestionIdAndTagId(@Param("questionId") Long questionId, @Param("tagId") Long tagId);

    /**
     * 删除标签的所有映射
     */
    @Modifying
    @Query("DELETE FROM QuestionTagMapping m WHERE m.tag.id = :tagId")
    void deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 删除题目的所有映射
     */
    @Modifying
    @Query("DELETE FROM QuestionTagMapping m WHERE m.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);

    /**
     * 统计标签关联的题目数量
     */
    @Query("SELECT COUNT(DISTINCT m.question.id) FROM QuestionTagMapping m WHERE m.tag.id = :tagId")
    long countQuestionsByTagId(@Param("tagId") Long tagId);

    /**
     * 统计题目关联的标签数量
     */
    @Query("SELECT COUNT(DISTINCT m.tag.id) FROM QuestionTagMapping m WHERE m.question.id = :questionId")
    long countTagsByQuestionId(@Param("questionId") Long questionId);
} 