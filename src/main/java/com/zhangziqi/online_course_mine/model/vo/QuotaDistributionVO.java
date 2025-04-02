package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配额分布VO（用于饼图展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaDistributionVO {
    /**
     * 配额类型
     */
    private String type;
    
    /**
     * 配额类型名称
     */
    private String name;
    
    /**
     * 已使用配额（字节）
     */
    private Long usedQuota;
    
    /**
     * 占总使用量的百分比
     */
    private Double percentage;
} 