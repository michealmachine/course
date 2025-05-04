package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动类型统计视图对象
 * 用于统计不同活动类型的学习时长
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTypeStatVO {

    /**
     * 活动类型代码
     */
    private String activityType;

    /**
     * 活动类型描述
     */
    private String activityTypeDescription;

    /**
     * 学习总时长（秒）
     */
    private Long totalDurationSeconds;

    /**
     * 活动次数
     */
    private Integer activityCount;

    /**
     * 占总时长的百分比
     */
    private Double percentage;
}