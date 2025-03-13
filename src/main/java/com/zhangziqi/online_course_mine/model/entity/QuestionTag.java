package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 题目标签实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_tags")
@EqualsAndHashCode(callSuper = true)
public class QuestionTag extends BaseEntity {

    /**
     * 标签名称
     */
    @Column(nullable = false, length = 50)
    private String name;

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