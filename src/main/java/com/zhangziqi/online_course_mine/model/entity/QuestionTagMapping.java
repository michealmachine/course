package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 题目-标签映射实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_tag_mappings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "tag_id"}))
@EqualsAndHashCode(callSuper = true)
public class QuestionTagMapping extends BaseEntity {

    /**
     * 题目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 标签
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private QuestionTag tag;
    
    /**
     * 获取题目ID
     */
    public Long getQuestionId() {
        return question != null ? question.getId() : null;
    }
    
    /**
     * 获取标签ID
     */
    public Long getTagId() {
        return tag != null ? tag.getId() : null;
    }
} 