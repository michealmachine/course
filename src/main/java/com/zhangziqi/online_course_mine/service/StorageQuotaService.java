package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.StorageQuotaVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 存储配额服务接口
 */
public interface StorageQuotaService {
    
    /**
     * 检查机构是否有足够的配额上传指定大小的文件
     *
     * @param institutionId 机构ID
     * @param type 配额类型
     * @param fileSize 文件大小(字节)
     * @return 是否有足够配额
     */
    boolean hasEnoughQuota(Long institutionId, QuotaType type, Long fileSize);
    
    /**
     * 获取机构指定类型的配额信息
     *
     * @param institutionId 机构ID
     * @param type 配额类型
     * @return 配额信息
     */
    StorageQuotaVO getQuotaInfo(Long institutionId, QuotaType type);
    
    /**
     * 获取机构所有类型的配额
     *
     * @param institutionId 机构ID
     * @return 配额列表
     */
    List<StorageQuotaVO> getAllQuotas(Long institutionId);
    
    /**
     * 更新已使用的配额
     *
     * @param institutionId 机构ID
     * @param type 配额类型
     * @param sizeDelta 大小变化(字节)，正数表示增加，负数表示减少
     */
    void updateUsedQuota(Long institutionId, QuotaType type, Long sizeDelta);
    
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