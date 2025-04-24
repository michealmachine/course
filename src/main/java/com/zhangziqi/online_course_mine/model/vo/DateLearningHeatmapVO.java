package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 日期学习热力图视图对象
 * 用于展示按具体日期分组的学习时长统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateLearningHeatmapVO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 热力图数据
     * 按日期分组的学习时长统计
     * 键: 日期字符串 (yyyy-MM-dd格式)
     * 值: 该日期的学习时长(秒)
     */
    private Map<String, Integer> heatmapData;

    /**
     * 最大学习时长(秒)
     * 用于前端计算热度颜色
     */
    private Integer maxActivityCount; // 字段名保持不变，但含义已更改为最大学习时长
}
