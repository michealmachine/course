package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 题目组项实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_group_items", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "question_id"}))
@EqualsAndHashCode(callSuper = true)
public class QuestionGroupItem extends BaseEntity {

    /**
     * 题目组
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private QuestionGroup group;

    /**
     * 题目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 在组中的顺序
     */
    @Column(nullable = false)
    private Integer orderIndex;
    
    /**
     * 难度级别（可覆盖题目原始难度）
     */
    private Integer difficulty;
    
    /**
     * 分值（可覆盖题目原始分值）
     */
    private Integer score;
    
    /**
     * 获取题目组ID
     */
    public Long getGroupId() {
        return group != null ? group.getId() : null;
    }
    
    /**
     * 获取题目ID
     */
    public Long getQuestionId() {
        return question != null ? question.getId() : null;
    }
} 