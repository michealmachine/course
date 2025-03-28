package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.QuotaApplicationDTO;
import com.zhangziqi.online_course_mine.model.vo.QuotaApplicationVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.QuotaApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储配额申请控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quota-applications")
@Tag(name = "存储配额申请", description = "存储配额申请相关API")
@RequiredArgsConstructor
@Validated
public class QuotaApplicationController {
    
    private final QuotaApplicationService quotaApplicationService;
    
    /**
     * 申请增加存储配额
     */
    @PostMapping("/apply")
    @Operation(summary = "申请增加存储配额", description = "机构用户申请增加存储配额")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<Map<String, String>> applyQuota(@RequestBody @Valid QuotaApplicationDTO dto) {
        String username = SecurityUtil.getCurrentUsername();
        
        if (SecurityUtil.getCurrentInstitutionId() == null) {
            return Result.fail("用户未关联机构");
        }
        
        String applicationId = quotaApplicationService.applyQuota(username, dto);
        
        Map<String, String> result = new HashMap<>();
        result.put("applicationId", applicationId);
        return Result.success(result);
    }
    
    /**
     * 查询申请状态
     */
    @GetMapping("/status/{applicationId}")
    @Operation(summary = "查询申请状态", description = "查询自己提交的申请状态")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<QuotaApplicationVO> getApplicationStatus(
            @PathVariable("applicationId") @NotBlank String applicationId) {
        String username = SecurityUtil.getCurrentUsername();
        
        QuotaApplicationVO vo = quotaApplicationService.getApplicationStatus(username, applicationId);
        return Result.success(vo);
    }
    
    /**
     * 获取用户的申请列表
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户申请列表", description = "获取当前用户的申请列表")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<Page<QuotaApplicationVO>> getUserApplications(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        String username = SecurityUtil.getCurrentUsername();
        
        Page<QuotaApplicationVO> page = quotaApplicationService.getUserApplications(username, status, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取机构的申请列表（机构管理员可查看）
     */
    @GetMapping("/institution")
    @Operation(summary = "获取机构申请列表", description = "获取当前机构的申请列表，仅机构管理员可查看")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<Page<QuotaApplicationVO>> getInstitutionApplications(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        String username = SecurityUtil.getCurrentUsername();
        
        if (!SecurityUtil.isInstitutionAdmin()) {
            return Result.fail(403, "权限不足，只有机构管理员可以查看机构配额申请");
        }
        
        Page<QuotaApplicationVO> page = quotaApplicationService.getInstitutionApplications(username, status, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取所有申请列表（管理员可查看）
     */
    @GetMapping("/admin")
    @Operation(summary = "获取所有申请列表", description = "获取所有机构的申请列表，仅管理员可查看")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<QuotaApplicationVO>> getAllApplications(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        
        Page<QuotaApplicationVO> page = quotaApplicationService.getAllApplications(status, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取申请详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取申请详情", description = "获取指定申请的详情")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public Result<QuotaApplicationVO> getApplicationDetail(@PathVariable("id") @NotNull Long id) {
        
        // 权限检查
        if (!SecurityUtil.isAdmin()) {
            // 非管理员需要额外检查是否为机构管理员
            String username = SecurityUtil.getCurrentUsername();
            Long institutionId = SecurityUtil.getCurrentInstitutionId();
            
            if (institutionId == null) {
                return Result.fail(403, "权限不足，用户未关联机构");
            }
            
            if (!SecurityUtil.isInstitutionAdmin()) {
                return Result.fail(403, "权限不足，只有机构管理员可以查看申请详情");
            }
        }
        
        QuotaApplicationVO vo = quotaApplicationService.getApplicationDetail(id);
        return Result.success(vo);
    }
    
    /**
     * 审核通过申请（仅管理员）
     */
    @PostMapping("/approve/{id}")
    @Operation(summary = "审核通过申请", description = "审核通过指定的申请，仅管理员可操作")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<QuotaApplicationVO> approveApplication(@PathVariable("id") @NotNull Long id) {
        String username = SecurityUtil.getCurrentUsername();
        
        QuotaApplicationVO vo = quotaApplicationService.approveApplication(id, username);
        return Result.success(vo);
    }
    
    /**
     * 审核拒绝申请（仅管理员）
     */
    @PostMapping("/reject/{id}")
    @Operation(summary = "审核拒绝申请", description = "审核拒绝指定的申请，仅管理员可操作")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> rejectApplication(
            @PathVariable("id") @NotNull Long id,
            @RequestParam("reason") @NotBlank String reason) {
        String username = SecurityUtil.getCurrentUsername();
        
        quotaApplicationService.rejectApplication(id, reason, username);
        return Result.success();
    }
} 