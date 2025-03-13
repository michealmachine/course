package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目组实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_groups")
@EqualsAndHashCode(callSuper = true)
public class QuestionGroup extends BaseEntity {

    /**
     * 题目组名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 题目组描述
     */
    @Column(length = 500)
    private String description;

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
     * 题目组项列表
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<QuestionGroupItem> items = new ArrayList<>();
    
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
     * 添加题目组项
     */
    public void addItem(QuestionGroupItem item) {
        items.add(item);
        item.setGroup(this);
    }
    
    /**
     * 移除题目组项
     */
    public void removeItem(QuestionGroupItem item) {
        items.remove(item);
        item.setGroup(null);
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