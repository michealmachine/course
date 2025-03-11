package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 存储配额信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaInfoVO {
    /**
     * 配额类型
     */
    private String type;
    
    /**
     * 配额类型名称
     */
    private String typeName;
    
    /**
     * 总配额（字节）
     */
    private Long totalQuota;
    
    /**
     * 已用配额（字节）
     */
    private Long usedQuota;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdatedTime;
    
    /**
     * 获取可用配额
     */
    public Long getAvailableQuota() {
        return totalQuota - usedQuota;
    }
    
    /**
     * 获取使用百分比
     */
    public Double getUsagePercentage() {
        if (totalQuota == null || totalQuota == 0) {
            return 0.0;
        }
        return (double) usedQuota / totalQuota * 100.0;
    }
} 