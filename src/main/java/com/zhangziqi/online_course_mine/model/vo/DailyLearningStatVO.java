package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每日学习统计视图对象
 * 用于热图等数据可视化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLearningStatVO {
    
    /**
     * 日期（yyyy-MM-dd格式）
     */
    private String date;
    
    /**
     * 学习时长（秒）
     */
    private Long durationSeconds;
    
    /**
     * 学习活动次数
     */
    private Integer activityCount;
} 