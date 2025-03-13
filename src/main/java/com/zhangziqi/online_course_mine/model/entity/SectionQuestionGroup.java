package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 课程小节-题目组关联实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "section_question_groups", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "question_group_id"}))
@EqualsAndHashCode(callSuper = true)
public class SectionQuestionGroup extends BaseEntity {

    /**
     * 课程小节ID
     */
    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    /**
     * 题目组
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_group_id", nullable = false)
    private QuestionGroup questionGroup;
    
    /**
     * 在小节中的顺序
     */
    @Column(nullable = false)
    private Integer orderIndex;
    
    /**
     * 是否随机题目顺序
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean randomOrder = false;
    
    /**
     * 是否按难度顺序排序
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean orderByDifficulty = false;
    
    /**
     * 是否显示答案解析
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean showAnalysis = true;
    
    /**
     * 获取题目组ID
     */
    public Long getQuestionGroupId() {
        return questionGroup != null ? questionGroup.getId() : null;
    }
} 