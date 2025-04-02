package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 存储配额统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaStatsVO {
    /**
     * 总体配额使用情况
     */
    private QuotaInfoVO totalQuota;
    
    /**
     * 各类型配额使用情况
     */
    private List<QuotaInfoVO> typeQuotas;
    
    /**
     * 配额类型分布数据（用于饼图）
     */
    private List<QuotaDistributionVO> distribution;
} 