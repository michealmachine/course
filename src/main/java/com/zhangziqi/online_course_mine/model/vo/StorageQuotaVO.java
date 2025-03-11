package com.zhangziqi.online_course_mine.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 存储配额VO
 */
@Data
public class StorageQuotaVO {
    
    /**
     * 配额ID
     */
    private Long id;
    
    /**
     * 配额类型
     */
    private String type;
    
    /**
     * 总配额大小(字节)
     */
    private Long totalQuota;
    
    /**
     * 已使用配额(字节)
     */
    private Long usedQuota;
    
    /**
     * 可用配额(字节)
     */
    private Long availableQuota;
    
    /**
     * 使用百分比
     */
    private double usagePercentage;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 机构ID
     */
    private Long institutionId;
} 