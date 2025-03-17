package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 课程评论统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsVO {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 平均评分
     */
    private Float averageRating;
    
    /**
     * 评分人数
     */
    private Integer ratingCount;
    
    /**
     * 各分数段人数
     * key: 评分(1-5)
     * value: 该评分的人数
     */
    private Map<Integer, Integer> ratingDistribution;
} 