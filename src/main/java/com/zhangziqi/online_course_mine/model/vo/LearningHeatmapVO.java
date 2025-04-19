package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 学习热力图视图对象
 * 用于展示按星期几和小时分组的学习活动统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningHeatmapVO {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 热力图数据
     * 按小时和星期几分组的学习活动统计
     */
    private Map<Integer, Map<Integer, Integer>> heatmapData; // 外层Map: 星期几(1-7) -> 内层Map: 小时(0-23) -> 活动次数
    
    /**
     * 最大活动次数
     * 用于前端计算热度颜色
     */
    private Integer maxActivityCount;
}
