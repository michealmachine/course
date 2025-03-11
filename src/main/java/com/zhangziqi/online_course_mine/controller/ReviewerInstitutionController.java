package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.InstitutionApplicationQueryDTO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionApplicationVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 审核员机构审核控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviewer/institutions")
@PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
@Tag(name = "机构审核", description = "机构申请审核相关接口")
public class ReviewerInstitutionController {
    
    private final InstitutionService institutionService;
    
    /**
     * 分页查询机构申请
     *
     * @param queryDTO 查询参数
     * @return 申请分页
     */
    @GetMapping("/applications")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "分页查询机构申请", description = "分页查询机构申请列表，可按状态筛选")
    public Result<Page<InstitutionApplicationVO>> getApplications(@Valid InstitutionApplicationQueryDTO queryDTO) {
        log.info("分页查询机构申请: {}", queryDTO);
        return Result.success(
            institutionService.getApplications(queryDTO)
        );
    }
    
    /**
     * 查询申请详情
     *
     * @param id 申请ID
     * @return 申请详情
     */
    @GetMapping("/applications/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "查询申请详情", description = "根据ID查询机构申请详情")
    public Result<InstitutionApplicationVO> getApplicationDetail(
            @Parameter(description = "申请ID") @PathVariable Long id) {
        log.info("查询申请详情: {}", id);
        return Result.success(
            institutionService.getApplicationDetail(id)
        );
    }
    
    /**
     * 审核通过申请
     *
     * @param id 申请ID
     * @param authentication 认证信息
     * @return 机构信息
     */
    @PostMapping("/applications/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "审核通过", description = "通过机构申请，自动创建机构并生成注册码")
    public Result<InstitutionVO> approveApplication(
            @Parameter(description = "申请ID") @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("审核通过申请: {}, 审核人: {}", id, username);
        return Result.success(
            institutionService.approveApplication(id, username)
        );
    }
    
    /**
     * 审核拒绝申请
     *
     * @param id 申请ID
     * @param reason 拒绝原因
     * @param authentication 认证信息
     * @return 结果
     */
    @PostMapping("/applications/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "审核拒绝", description = "拒绝机构申请")
    public Result<Void> rejectApplication(
            @Parameter(description = "申请ID") @PathVariable Long id,
            @Parameter(description = "拒绝原因") @RequestParam String reason,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("审核拒绝申请: {}, 审核人: {}, 原因: {}", id, username, reason);
        institutionService.rejectApplication(id, reason, username);
        return Result.success();
    }
} 