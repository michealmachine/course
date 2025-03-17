package com.zhangziqi.online_course_mine.model.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程评论查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewQueryDTO {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 排序方式
     * newest - 最新
     * highest_rating - 评分从高到低
     * lowest_rating - 评分从低到高
     */
    @Builder.Default
    private String orderBy = "newest";
    
    /**
     * 筛选特定评分
     */
    private Integer ratingFilter;
} 