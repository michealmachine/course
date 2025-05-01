package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.ReviewRecord;
import com.zhangziqi.online_course_mine.model.enums.ReviewResult;
import com.zhangziqi.online_course_mine.model.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审核记录视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRecordVO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 审核类型
     */
    private Integer reviewType;
    
    /**
     * 审核结果
     */
    private Integer result;
    
    /**
     * 审核对象ID
     */
    private Long targetId;
    
    /**
     * 审核对象名称
     */
    private String targetName;
    
    /**
     * 审核人ID
     */
    private Long reviewerId;
    
    /**
     * 审核人姓名
     */
    private String reviewerName;
    
    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;
    
    /**
     * 审核意见
     */
    private String comment;
    
    /**
     * 机构ID（可选，用于机构审核）
     */
    private Long institutionId;
    
    /**
     * 发布版本ID（可选，用于课程审核）
     */
    private Long publishedVersionId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为VO
     */
    public static ReviewRecordVO fromEntity(ReviewRecord entity) {
        if (entity == null) {
            return null;
        }
        
        return ReviewRecordVO.builder()
                .id(entity.getId())
                .reviewType(entity.getReviewType())
                .result(entity.getResult())
                .targetId(entity.getTargetId())
                .targetName(entity.getTargetName())
                .reviewerId(entity.getReviewerId())
                .reviewerName(entity.getReviewerName())
                .reviewedAt(entity.getReviewedAt())
                .comment(entity.getComment())
                .institutionId(entity.getInstitutionId())
                .publishedVersionId(entity.getPublishedVersionId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
