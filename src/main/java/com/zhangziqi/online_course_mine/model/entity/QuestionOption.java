package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 题目选项实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_options")
@EqualsAndHashCode(callSuper = true)
public class QuestionOption extends BaseEntity {

    /**
     * 所属题目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 选项内容
     */
    @Column(nullable = false, length = 1000)
    private String content;

    /**
     * 是否正确选项
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean isCorrect = false;

    /**
     * 排序序号
     */
    @Column(nullable = false)
    private Integer orderIndex;
} 