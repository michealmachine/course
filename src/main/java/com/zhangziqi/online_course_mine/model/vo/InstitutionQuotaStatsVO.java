package com.zhangziqi.online_course_mine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 机构配额统计视图对象
 * 用于管理员查看所有机构配额使用情况
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "机构配额统计")
public class InstitutionQuotaStatsVO {
    
    /**
     * 所有机构的总配额使用情况
     */
    @Schema(description = "所有机构的总配额使用情况")
    private TotalQuotaUsageVO totalUsage;
    
    /**
     * 各机构配额使用情况列表
     */
    @Schema(description = "各机构配额使用情况列表")
    private List<InstitutionQuotaVO> institutions;
    
    /**
     * 机构配额分布（用于饼图）
     */
    @Schema(description = "机构配额分布数据（用于饼图）")
    private List<InstitutionQuotaDistributionVO> distribution;
    
    /**
     * 总体配额使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "总体配额使用情况")
    public static class TotalQuotaUsageVO {
        /**
         * 所有机构总配额（字节）
         */
        @Schema(description = "所有机构总配额（字节）")
        private Long totalQuota;
        
        /**
         * 所有机构已用配额（字节）
         */
        @Schema(description = "所有机构已用配额（字节）")
        private Long usedQuota;
        
        /**
         * 所有机构可用配额（字节）
         */
        @Schema(description = "所有机构可用配额（字节）")
        private Long availableQuota;
        
        /**
         * 使用百分比
         */
        @Schema(description = "使用百分比")
        private Double usagePercentage;
        
        /**
         * 机构数量
         */
        @Schema(description = "机构数量")
        private Integer institutionCount;
    }
    
    /**
     * 机构配额使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "机构配额使用情况")
    public static class InstitutionQuotaVO {
        /**
         * 机构ID
         */
        @Schema(description = "机构ID")
        private Long institutionId;
        
        /**
         * 机构名称
         */
        @Schema(description = "机构名称")
        private String institutionName;
        
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
         * 上次更新时间
         */
        @Schema(description = "上次更新时间")
        private String lastUpdatedTime;
    }
    
    /**
     * 机构配额分布（用于饼图）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "机构配额分布")
    public static class InstitutionQuotaDistributionVO {
        /**
         * 机构ID
         */
        @Schema(description = "机构ID")
        private Long institutionId;
        
        /**
         * 机构名称
         */
        @Schema(description = "机构名称")
        private String institutionName;
        
        /**
         * 已使用配额（字节）
         */
        @Schema(description = "已使用配额（字节）")
        private Long usedQuota;
        
        /**
         * 占总使用量的百分比
         */
        @Schema(description = "占总使用量的百分比")
        private Double percentage;
    }
} 