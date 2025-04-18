package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计综合VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsVO {
    
    /**
     * 用户角色分布统计
     */
    private UserRoleDistributionVO roleDistribution;
    
    /**
     * 用户增长统计
     */
    private UserGrowthStatsVO growthStats;
    
    /**
     * 用户状态统计
     */
    private UserStatusStatsVO statusStats;
    
    /**
     * 用户活跃度统计
     */
    private UserActivityStatsVO activityStats;
} 