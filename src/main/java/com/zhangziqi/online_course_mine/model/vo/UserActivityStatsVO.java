package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 用户活跃度统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityStatsVO {
    
    /**
     * 总用户数
     */
    private Long totalUserCount;
    
    /**
     * 活跃用户数（过去30天有登录记录）
     */
    private Long activeUserCount;
    
    /**
     * 非活跃用户数（过去30天无登录记录）
     */
    private Long inactiveUserCount;
    
    /**
     * 今日活跃用户数
     */
    private Long todayActiveUsers;
    
    /**
     * 过去7天活跃用户数
     */
    private Long weekActiveUsers;
    
    /**
     * 过去30天活跃用户数
     */
    private Long monthActiveUsers;
    
    /**
     * 活跃用户占比
     */
    private Double activeUserPercentage;
    
    /**
     * 每日活跃用户数据（过去30天）
     */
    private List<DailyActiveUsers> dailyActiveUsers;
    
    /**
     * 用户活跃时间分布（一天24小时）
     */
    private Map<Integer, Long> hourlyActiveDistribution;
    
    /**
     * 用户活跃时间分布（一周7天）
     */
    private Map<Integer, Long> weekdayActiveDistribution;
    
    /**
     * 每日活跃用户数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyActiveUsers {
        /**
         * 日期（yyyy-MM-dd格式）
         */
        private String date;
        
        /**
         * 活跃用户数
         */
        private Long count;
    }
} 