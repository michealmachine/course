package com.zhangziqi.online_course_mine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "存储配额信息")
public class QuotaInfoVO {
    /**
     * 配额类型
     */
    @Schema(description = "配额类型")
    private String type;
    
    /**
     * 配额类型名称
     */
    @Schema(description = "配额类型名称")
    private String typeName;
    
    /**
     * 总配额（字节）
     */
    @Schema(description = "总配额（字节）")
    private Long totalQuota;
    
    /**
     * 已用配额（字节）
     */
    @Schema(description = "已用配额（字节）")
    private Long usedQuota;
    
    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdatedTime;
    
    /**
     * 可用配额（字节）
     */
    @Schema(description = "可用配额（字节）")
    private Long availableQuota;
    
    /**
     * 使用百分比
     */
    @Schema(description = "使用百分比")
    private Double usagePercentage;
    
    /**
     * 获取可用配额
     */
    public Long getAvailableQuota() {
        if (availableQuota != null) {
            return availableQuota;
        }
        return totalQuota != null && usedQuota != null ? 
            Math.max(0, totalQuota - usedQuota) : 0L;
    }
    
    /**
     * 获取使用百分比
     */
    public Double getUsagePercentage() {
        if (usagePercentage != null) {
            return usagePercentage;
        }
        if (totalQuota == null || totalQuota == 0) {
            return 0.0;
        }
        return (double) usedQuota / totalQuota * 100.0;
    }
} 