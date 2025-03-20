package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 学习统计数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningStatisticsVO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 学习课程总数
     */
    private Integer totalCourses;
    
    /**
     * 完成课程数
     */
    private Integer completedCourses;
    
    /**
     * 总学习时长（秒）
     */
    private Long totalLearningDuration;
    
    /**
     * 今日学习时长（秒）
     */
    private Long todayLearningDuration;
    
    /**
     * 本周学习时长（秒）
     */
    private Long weekLearningDuration;
    
    /**
     * 本月学习时长（秒）
     */
    private Long monthLearningDuration;
    
    /**
     * 学习天数
     */
    private Integer learningDays;
    
    /**
     * 最长连续学习天数
     */
    private Integer maxConsecutiveDays;
    
    /**
     * 当前连续学习天数
     */
    private Integer currentConsecutiveDays;
    
    /**
     * 题目总数
     */
    private Integer totalQuestions;
    
    /**
     * 正确题目数
     */
    private Integer correctQuestions;
    
    /**
     * 错题数
     */
    private Integer wrongQuestions;
    
    /**
     * 课程学习统计
     */
    @Builder.Default
    private List<CourseStatisticsVO> courseStatistics = new ArrayList<>();
    
    /**
     * 每日学习时长统计（过去30天）
     */
    @Builder.Default
    private List<DailyLearningVO> dailyLearning = new ArrayList<>();
    
    /**
     * 课程学习统计VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStatisticsVO {
        
        /**
         * 课程ID
         */
        private Long courseId;
        
        /**
         * 课程标题
         */
        private String courseTitle;
        
        /**
         * 课程封面
         */
        private String courseCover;
        
        /**
         * 学习进度（百分比）
         */
        private Integer progress;
        
        /**
         * 学习时长（秒）
         */
        private Long learningDuration;
        
        /**
         * 最后学习时间戳
         */
        private Long lastLearnTime;
    }
    
    /**
     * 每日学习统计VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyLearningVO {
        
        /**
         * 日期（yyyy-MM-dd格式）
         */
        private String date;
        
        /**
         * 学习时长（秒）
         */
        private Long duration;
    }
} 