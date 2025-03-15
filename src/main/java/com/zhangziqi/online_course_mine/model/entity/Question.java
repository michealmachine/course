package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试题实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
@EqualsAndHashCode(callSuper = true)
public class Question extends BaseEntity {

    /**
     * 题目标题/简称
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 题目内容/题干
     */
    @Column(nullable = false, length = 2000)
    private String content;

    /**
     * 题目类型
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer type = QuestionType.SINGLE_CHOICE.getValue();

    /**
     * 难度级别（1-5）
     */
    @Column(nullable = false)
    private Integer difficulty;

    /**
     * 分值
     */
    @Column(nullable = false)
    private Integer score;

    /**
     * 解析
     */
    @Column(length = 2000)
    private String analysis;

    /**
     * 正确答案（填空题必填，简答题可选）
     */
    @Column(length = 2000)
    private String answer;

    /**
     * 所属机构
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 创建者名称
     */
    @Column(name = "creator_name", length = 100)
    private String creatorName;
    
    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 选项列表
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();

    /**
     * 获取机构ID
     */
    public Long getInstitutionId() {
        return institution != null ? institution.getId() : null;
    }

    /**
     * 设置机构ID
     */
    public void setInstitutionId(Long institutionId) {
        if (institutionId != null) {
            this.institution = Institution.builder().id(institutionId).build();
        } else {
            this.institution = null;
        }
    }

    /**
     * 获取题目类型枚举
     */
    @Transient
    public QuestionType getTypeEnum() {
        return QuestionType.getByValue(this.type);
    }

    /**
     * 设置题目类型枚举
     */
    public void setTypeEnum(QuestionType questionType) {
        this.type = questionType != null ? questionType.getValue() : null;
    }
    
    /**
     * 添加选项
     */
    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }
    
    /**
     * 移除选项
     */
    public void removeOption(QuestionOption option) {
        options.remove(option);
        option.setQuestion(null);
    }
    
    /**
     * 保存前操作
     */
    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = this.createdTime;
    }
    
    /**
     * 更新前操作
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
} 