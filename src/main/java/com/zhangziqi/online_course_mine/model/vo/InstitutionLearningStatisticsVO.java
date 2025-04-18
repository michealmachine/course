package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 机构学习统计数据VO
 * 用于向机构展示学习反馈数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionLearningStatisticsVO {
    
    /**
     * 机构ID
     */
    private Long institutionId;
    
    /**
     * 机构名称
     */
    private String institutionName;
    
    /**
     * 总学习人数
     * 在该机构课程中有学习记录的不同用户数量
     */
    private Long totalLearners;
    
    /**
     * 总学习课程数
     * 该机构有学习记录的课程数量
     */
    private Integer totalActiveCourses;
    
    /**
     * 总学习时长（秒）
     * 所有用户在该机构课程中的学习时长总和
     */
    private Long totalLearningDuration;
    
    /**
     * 今日学习时长（秒）
     * 今日所有用户在该机构课程中的学习时长总和
     */
    private Long todayLearningDuration;
    
    /**
     * 本周学习时长（秒）
     * 本周所有用户在该机构课程中的学习时长总和
     */
    private Long weekLearningDuration;
    
    /**
     * 本月学习时长（秒）
     * 本月所有用户在该机构课程中的学习时长总和
     */
    private Long monthLearningDuration;
    
    /**
     * 总题目尝试次数
     * 所有用户在该机构课程中尝试回答题目的总次数
     */
    private Integer totalQuestionAttempts;
    
    /**
     * 课程学习统计列表
     * 按课程分组的学习统计信息
     */
    @Builder.Default
    private List<CourseStatisticsVO> courseStatistics = new ArrayList<>();
    
    /**
     * 每日学习统计（过去30天）
     * 按日期分组的学习时长和活动次数
     */
    @Builder.Default
    private List<DailyLearningStatVO> dailyLearning = new ArrayList<>();
    
    /**
     * 活动类型统计
     * 按学习活动类型分组的统计信息
     */
    @Builder.Default
    private List<ActivityTypeStatVO> activityTypeStats = new ArrayList<>();
    
    /**
     * 最活跃用户统计
     * 学习时长最长的用户列表
     */
    @Builder.Default
    private List<ActiveUserVO> mostActiveUsers = new ArrayList<>();
    
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
         * 学习人数
         * 在该课程中有学习记录的不同用户数量
         */
        private Long learnerCount;
        
        /**
         * 总学习时长（秒）
         * 所有用户在该课程中的学习时长总和
         */
        private Long totalDuration;
        
        /**
         * 学习活动次数
         * 所有用户在该课程中的学习活动总次数
         */
        private Integer activityCount;
        
        /**
         * 完成人数
         * 学习进度达到100%的用户数量
         */
        private Long completionCount;
        
        /**
         * 平均学习进度
         * 所有学习该课程的用户的平均学习进度（百分比）
         */
        private Double averageProgress;
    }
    
    /**
     * 活跃用户VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveUserVO {
        
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 学习时长（秒）
         */
        private Long learningDuration;
        
        /**
         * 学习活动次数
         */
        private Integer activityCount;
    }
} 