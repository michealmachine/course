package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户增长统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthStatsVO {

    /**
     * 总用户数
     */
    private Long totalUserCount;
    
    /**
     * 今日新增用户数
     */
    private Long todayNewUsers;
    
    /**
     * 本周新增用户数
     */
    private Long weekNewUsers;
    
    /**
     * 本月新增用户数
     */
    private Long monthNewUsers;
    
    /**
     * 日增长率
     */
    private Double dailyGrowthRate;
    
    /**
     * 周增长率
     */
    private Double weeklyGrowthRate;
    
    /**
     * 月增长率
     */
    private Double monthlyGrowthRate;
    
    /**
     * 每日用户注册数据（过去30天）
     */
    private List<DailyRegistration> dailyRegistrations;
    
    /**
     * 每日用户注册数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRegistration {
        /**
         * 日期（yyyy-MM-dd格式）
         */
        private String date;
        
        /**
         * 注册用户数
         */
        private Long count;
    }
} 