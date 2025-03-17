package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.CourseVersion;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 课程实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
@EqualsAndHashCode(callSuper = true)
public class Course extends BaseEntity {

    /**
     * 课程标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 课程描述
     */
    @Column(length = 2000)
    private String description;

    /**
     * 封面图片
     */
    @Column(length = 255)
    private String coverImage;

    /**
     * 课程状态
     */
    @Builder.Default
    private Integer status = CourseStatus.DRAFT.getValue();

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
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;

    /**
     * 审核意见
     */
    @Column(length = 1000)
    private String reviewComment;

    /**
     * 发布版本ID（指向当前发布版本的课程ID）
     */
    private Long publishedVersionId;

    /**
     * 是否为发布版本
     */
    @Builder.Default
    private Boolean isPublishedVersion = false;

    /**
     * 版本号
     */
    @Builder.Default
    private Integer version = 1;

    /**
     * 版本类型
     */
    @Builder.Default
    private Integer versionType = CourseVersion.DRAFT.getValue();

    /**
     * 支付类型（免费/付费）
     */
    @Builder.Default
    private Integer paymentType = CoursePaymentType.FREE.getValue();

    /**
     * 课程价格
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 折扣价格
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;

    /**
     * 难度级别 (1-初级, 2-中级, 3-高级)
     */
    private Integer difficulty;

    /**
     * 总课时数
     */
    private Integer totalLessons;

    /**
     * 总时长(分钟)
     */
    private Integer totalDuration;

    /**
     * 学习人数
     */
    @Builder.Default
    private Integer studentCount = 0;

    /**
     * 平均评分(1-5星)
     */
    @Column
    private Float averageRating;

    /**
     * 评分人数
     */
    @Builder.Default
    private Integer ratingCount = 0;

    /**
     * 适合人群
     */
    @Column(length = 1000)
    private String targetAudience;

    /**
     * 学习目标
     */
    @Column(length = 1000)
    private String learningObjectives;

    /**
     * 课程分类
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 课程标签
     */
    @ManyToMany
    @JoinTable(
        name = "course_tags",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    /**
     * 课程章节
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();

    /**
     * 收藏此课程的用户
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserFavorite> favoriteUsers = new HashSet<>();

    /**
     * 乐观锁版本字段
     */
    @Version
    private Integer dataVersion;

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
     * 获取课程状态枚举
     */
    @Transient
    public CourseStatus getStatusEnum() {
        return CourseStatus.getByValue(this.status);
    }

    /**
     * 设置课程状态枚举
     */
    public void setStatusEnum(CourseStatus status) {
        this.status = status != null ? status.getValue() : null;
    }

    /**
     * 获取版本类型枚举
     */
    @Transient
    public CourseVersion getVersionTypeEnum() {
        return CourseVersion.getByValue(this.versionType);
    }

    /**
     * 设置版本类型枚举
     */
    public void setVersionTypeEnum(CourseVersion versionType) {
        this.versionType = versionType != null ? versionType.getValue() : null;
    }
    
    /**
     * 获取支付类型枚举
     */
    @Transient
    public CoursePaymentType getPaymentTypeEnum() {
        return CoursePaymentType.getByValue(this.paymentType);
    }
    
    /**
     * 设置支付类型枚举
     */
    public void setPaymentTypeEnum(CoursePaymentType paymentType) {
        this.paymentType = paymentType != null ? paymentType.getValue() : null;
    }
} 