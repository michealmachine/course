package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.ReviewResult;
import com.zhangziqi.online_course_mine.model.enums.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 审核记录实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review_records")
@EqualsAndHashCode(callSuper = true)
public class ReviewRecord extends BaseEntity {
    
    /**
     * 审核类型
     * @see ReviewType
     */
    @Column(name = "review_type", nullable = false)
    private Integer reviewType;
    
    /**
     * 审核结果
     * @see ReviewResult
     */
    @Column(name = "result", nullable = false)
    private Integer result;
    
    /**
     * 审核对象ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    /**
     * 审核对象名称
     */
    @Column(name = "target_name", nullable = false, length = 200)
    private String targetName;
    
    /**
     * 审核人ID
     */
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;
    
    /**
     * 审核人姓名
     */
    @Column(name = "reviewer_name", nullable = false, length = 100)
    private String reviewerName;
    
    /**
     * 审核时间
     */
    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
    
    /**
     * 审核意见
     */
    @Column(name = "comment", length = 1000)
    private String comment;
    
    /**
     * 机构ID（可选，用于机构审核）
     */
    @Column(name = "institution_id")
    private Long institutionId;
    
    /**
     * 发布版本ID（可选，用于课程审核）
     */
    @Column(name = "published_version_id")
    private Long publishedVersionId;
    
    /**
     * 获取审核类型枚举
     */
    @Transient
    public ReviewType getReviewTypeEnum() {
        return ReviewType.getByValue(this.reviewType);
    }
    
    /**
     * 设置审核类型枚举
     */
    public void setReviewTypeEnum(ReviewType reviewType) {
        this.reviewType = reviewType.getValue();
    }
    
    /**
     * 获取审核结果枚举
     */
    @Transient
    public ReviewResult getResultEnum() {
        return ReviewResult.getByValue(this.result);
    }
    
    /**
     * 设置审核结果枚举
     */
    public void setResultEnum(ReviewResult result) {
        this.result = result.getValue();
    }
}
