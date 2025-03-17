package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程评论区整体VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewSectionVO {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 评分统计
     */
    private ReviewStatsVO stats;
    
    /**
     * 评论列表
     */
    private List<ReviewVO> reviews;
    
    /**
     * 总评论数
     */
    private Integer totalReviews;
    
    /**
     * 当前页码
     */
    private Integer currentPage;
    
    /**
     * 总页数
     */
    private Integer totalPages;
} 