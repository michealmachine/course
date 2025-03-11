package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.QuotaInfoVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 存储配额服务接口
 * 用于管理机构的存储配额
 */
public interface StorageQuotaService {
    
    /**
     * 检查是否有足够的配额
     * 
     * @param institutionId 机构ID
     * @param quotaType 配额类型
     * @param requiredSize 所需空间大小（字节）
     * @return 是否有足够配额
     */
    boolean hasEnoughQuota(Long institutionId, QuotaType quotaType, Long requiredSize);
    
    /**
     * 更新已使用配额
     * 
     * @param institutionId 机构ID
     * @param quotaType 配额类型
     * @param deltaSize 变化量（字节，正数为增加，负数为减少）
     */
    void updateUsedQuota(Long institutionId, QuotaType quotaType, Long deltaSize);
    
    /**
     * 获取机构配额信息
     * 
     * @param institutionId 机构ID
     * @return 配额信息
     */
    QuotaInfoVO getQuotaInfo(Long institutionId);
    
    /**
     * 获取机构所有类型的配额
     *
     * @param institutionId 机构ID
     * @return 配额列表
     */
    List<QuotaInfoVO> getAllQuotas(Long institutionId);
    
    /**
     * 设置配额大小
     *
     * @param institutionId 机构ID
     * @param type 配额类型
     * @param totalQuota 总配额(字节)
     * @param expiresAt 过期时间(可选)
     */
    void setQuota(Long institutionId, QuotaType type, Long totalQuota, LocalDateTime expiresAt);
} 