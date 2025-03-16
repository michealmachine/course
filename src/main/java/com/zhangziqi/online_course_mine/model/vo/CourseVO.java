package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Tag;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.CourseVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseVO {

    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private Integer status;
    private Integer versionType;
    private Boolean isPublishedVersion;
    private Long creatorId;
    private String creatorName;
    private InstitutionVO institution;
    private CategoryVO category;
    private Set<TagVO> tags;
    private Integer paymentType;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer difficulty;
    private String targetAudience;
    private String learningObjectives;
    private Integer totalLessons;
    private Integer totalDuration;
    private Integer totalChapters;
    private Integer totalSections;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewStartedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime publishedAt;
    private String reviewComment;
    private Long reviewerId;
    private String reviewerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为VO
     */
    public static CourseVO fromEntity(Course course) {
        if (course == null) {
            return null;
        }
        
        // 构建CourseVO对象
        CourseVO.CourseVOBuilder builder = CourseVO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .coverUrl(course.getCoverImage())
                .status(course.getStatus())
                .versionType(course.getVersionType())
                .isPublishedVersion(course.getIsPublishedVersion())
                .creatorId(course.getCreatorId())
                .creatorName("")
                .paymentType(course.getPaymentType())
                .price(course.getPrice())
                .discountPrice(course.getDiscountPrice())
                .difficulty(course.getDifficulty())
                .targetAudience(course.getTargetAudience())
                .learningObjectives(course.getLearningObjectives())
                .totalLessons(course.getTotalLessons())
                .totalDuration(course.getTotalDuration())
                .totalChapters(course.getChapters() != null ? course.getChapters().size() : 0)
                .totalSections(0) // 这里需要根据章节计算小节总数
                .submittedAt(null) // 需要添加这些字段到Entity中或从其他地方获取
                .reviewStartedAt(null)
                .reviewedAt(course.getReviewedAt())
                .publishedAt(null)
                .reviewComment(course.getReviewComment())
                .reviewerId(course.getReviewerId())
                .reviewerName(null) // 需要添加这个字段到Entity中或从其他地方获取
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt());
        
        // 设置机构
        if (course.getInstitution() != null) {
            InstitutionVO institutionVO = new InstitutionVO();
            institutionVO.setId(course.getInstitution().getId());
            institutionVO.setName(course.getInstitution().getName());
            builder.institution(institutionVO);
        }
        
        // 设置分类
        if (course.getCategory() != null) {
            builder.category(CategoryVO.fromEntity(course.getCategory()));
        }
        
        // 设置标签
        if (course.getTags() != null && !course.getTags().isEmpty()) {
            Set<TagVO> tagVOs = course.getTags().stream()
                    .map(TagVO::fromEntity)
                    .collect(Collectors.toSet());
            builder.tags(tagVOs);
        } else {
            builder.tags(new HashSet<>());
        }
        
        return builder.build();
    }
    
    /**
     * 获取状态枚举
     */
    public CourseStatus getStatusEnum() {
        return CourseStatus.getByValue(this.status);
    }
    
    /**
     * 获取版本类型枚举
     */
    public CourseVersion getVersionTypeEnum() {
        return CourseVersion.getByValue(this.versionType);
    }
    
    /**
     * 获取付费类型枚举
     */
    public CoursePaymentType getPaymentTypeEnum() {
        return CoursePaymentType.getByValue(this.paymentType);
    }
} 