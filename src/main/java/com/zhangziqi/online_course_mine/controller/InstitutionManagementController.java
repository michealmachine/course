package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.InstitutionUpdateDTO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 机构管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/institution-management")
@Tag(name = "机构管理", description = "机构信息管理API")
@RequiredArgsConstructor
public class InstitutionManagementController {

    private final InstitutionService institutionService;

    /**
     * 获取机构详情
     */
    @GetMapping("/detail")
    @Operation(summary = "获取机构详情", description = "获取当前用户所属机构的详细信息")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<InstitutionVO> getInstitutionDetail() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        if (institutionId == null) {
            return Result.fail("用户未关联机构");
        }
        
        String username = SecurityUtil.getCurrentUsername();
        InstitutionVO institutionVO = institutionService.getInstitutionDetail(institutionId, username);
        return Result.success(institutionVO);
    }

    /**
     * 更新机构信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新机构信息", description = "更新当前用户所属机构的基本信息，仅机构管理员可操作")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<InstitutionVO> updateInstitution(@RequestBody @Valid InstitutionUpdateDTO updateDTO) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        if (institutionId == null) {
            return Result.fail("用户未关联机构");
        }
        
        // 检查用户是否为机构管理员
        if (!SecurityUtil.isInstitutionAdmin()) {
            return Result.fail(403, "权限不足，只有机构管理员可以更新机构信息");
        }
        
        InstitutionVO institutionVO = institutionService.updateInstitution(institutionId, updateDTO, username);
        return Result.success(institutionVO);
    }

    /**
     * 上传机构Logo
     */
    @PostMapping("/logo")
    @Operation(summary = "上传机构Logo", description = "上传并更新当前用户所属机构的Logo，仅机构管理员可操作")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<Map<String, String>> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        if (institutionId == null) {
            return Result.fail("用户未关联机构");
        }
        
        // 检查用户是否为机构管理员
        if (!SecurityUtil.isInstitutionAdmin()) {
            return Result.fail(403, "权限不足，只有机构管理员可以更新机构Logo");
        }
        
        InstitutionVO institutionVO = institutionService.updateInstitutionLogo(institutionId, file, username);
        
        Map<String, String> result = new HashMap<>();
        result.put("logoUrl", institutionVO.getLogo());
        return Result.success(result);
    }

    /**
     * 重置机构注册码
     */
    @PostMapping("/reset-register-code")
    @Operation(summary = "重置机构注册码", description = "重置当前用户所属机构的注册码，仅机构管理员可操作")
    @PreAuthorize("hasRole('INSTITUTION')")
    public Result<Map<String, String>> resetRegisterCode() {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        if (institutionId == null) {
            return Result.fail("用户未关联机构");
        }
        
        // 检查用户是否为机构管理员
        if (!SecurityUtil.isInstitutionAdmin()) {
            return Result.fail(403, "权限不足，只有机构管理员可以重置注册码");
        }
        
        String newRegisterCode = institutionService.resetInstitutionRegisterCode(institutionId, username);
        
        Map<String, String> result = new HashMap<>();
        result.put("registerCode", newRegisterCode);
        return Result.success(result);
    }
} 