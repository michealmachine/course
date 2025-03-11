package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.QuotaInfoVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 存储配额控制器
 */
@RestController
@RequestMapping("/api/storage/quota")
@RequiredArgsConstructor
public class StorageQuotaController {

    private final StorageQuotaService storageQuotaService;

    /**
     * 获取机构当前的配额信息
     *
     * @param institutionId 机构ID
     * @return 配额信息
     */
    @GetMapping("/{institutionId}")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<QuotaInfoVO> getQuotaInfo(@PathVariable Long institutionId) {
        // 注意：实际业务中需要验证当前用户是否属于该机构
        // 这里简化处理，假设前端传入的就是当前用户所属机构ID
        QuotaInfoVO quotaInfo = storageQuotaService.getQuotaInfo(institutionId);
        return Result.success(quotaInfo);
    }
    
    /**
     * 获取机构所有类型的配额信息
     *
     * @param institutionId 机构ID
     * @return 配额信息列表
     */
    @GetMapping("/{institutionId}/details")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<List<QuotaInfoVO>> getAllQuotas(@PathVariable Long institutionId) {
        // 同样需要验证当前用户是否属于该机构
        List<QuotaInfoVO> quotas = storageQuotaService.getAllQuotas(institutionId);
        return Result.success(quotas);
    }
} 