package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学习进度趋势视图对象
 * 用于展示课程学习进度随时间的变化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressTrendVO {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 趋势数据
     * 按日期分组的平均学习进度
     */
    private List<DailyProgressVO> progressData;
    
    /**
     * 每日进度数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyProgressVO {
        /**
         * 日期（yyyy-MM-dd格式）
         */
        private String date;
        
        /**
         * 平均学习进度（百分比）
         */
        private Double averageProgress;
        
        /**
         * 活跃学员数
         */
        private Integer activeUserCount;
    }
}
