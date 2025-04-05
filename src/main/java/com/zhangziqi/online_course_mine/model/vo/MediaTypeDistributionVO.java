package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 媒体类型分布统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaTypeDistributionVO {
    
    /**
     * 总媒体数量
     */
    private long totalCount;
    
    /**
     * 各类型媒体数量
     */
    private Map<MediaType, Long> typeCount;
    
    /**
     * 分布详情，用于图表展示
     */
    private List<TypeDistribution> distribution;
    
    /**
     * 类型分布详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeDistribution {
        /**
         * 媒体类型
         */
        private MediaType type;
        
        /**
         * 媒体类型显示名称
         */
        private String typeName;
        
        /**
         * 该类型媒体数量
         */
        private long count;
        
        /**
         * 占比
         */
        private double percentage;
    }
} 