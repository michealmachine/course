package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户状态统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusStatsVO {
    
    /**
     * 总用户数
     */
    private Long totalUserCount;
    
    /**
     * 正常状态用户数（status=1）
     */
    private Long activeUserCount;
    
    /**
     * 禁用状态用户数（status=0）
     */
    private Long disabledUserCount;
    
    /**
     * 正常用户占比
     */
    private Double activeUserPercentage;
    
    /**
     * 禁用用户占比
     */
    private Double disabledUserPercentage;
} 