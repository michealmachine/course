package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.InstitutionMemberService;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 机构成员管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/institutions/members")
@PreAuthorize("hasRole('INSTITUTION')")
@Tag(name = "机构成员管理", description = "机构成员管理相关接口")
public class InstitutionMemberController {
    
    private final InstitutionService institutionService;
    private final InstitutionMemberService memberService;
    
    /**
     * 获取机构注册码
     *
     * @param authentication 认证信息
     * @return 注册码
     */
    @GetMapping("/register-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构注册码", description = "获取当前用户所属机构的注册码")
    public Result<String> getRegisterCode(Authentication authentication) {
        String username = authentication.getName();
        log.info("获取机构注册码: {}", username);
        return Result.success(institutionService.getInstitutionRegisterCode(username));
    }
    
    /**
     * 获取机构成员列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键字
     * @return 成员列表
     */
    @GetMapping
    @Operation(summary = "获取机构成员列表", description = "分页获取机构成员列表，支持关键字搜索")
    public Result<Page<UserVO>> getMembers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        if (institutionId == null) {
            return Result.fail("用户未关联机构");
        }
        
        // 检查是否为机构管理员
        String username = SecurityUtil.getCurrentUsername();
        if (!institutionService.isInstitutionAdmin(username, institutionId)) {
            log.warn("非机构管理员尝试查看成员列表: username={}, institutionId={}", username, institutionId);
            return Result.fail(403, "权限不足，只有机构管理员可以查看成员列表");
        }
        
        log.info("获取机构成员列表: institutionId={}, pageNum={}, pageSize={}, keyword={}", 
                institutionId, pageNum, pageSize, keyword);
                
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<UserVO> members = memberService.getInstitutionMembers(institutionId, keyword, pageable);
        
        return Result.success(members);
    }
    
    /**
     * 获取机构成员统计信息
     *
     * @return 成员统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取机构成员统计信息", description = "获取当前机构的成员数量统计")
    public Result<Map<String, Object>> getMemberStats() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        if (institutionId == null) {
            return Result.fail("用户未关联机构");
        }
        
        // 检查是否为机构管理员
        String username = SecurityUtil.getCurrentUsername();
        if (!institutionService.isInstitutionAdmin(username, institutionId)) {
            log.warn("非机构管理员尝试查看成员统计: username={}, institutionId={}", username, institutionId);
            return Result.fail(403, "权限不足，只有机构管理员可以查看成员统计");
        }
        
        log.info("获取机构成员统计信息: institutionId={}", institutionId);
        Map<String, Object> stats = memberService.getMemberStats(institutionId);
        
        return Result.success(stats);
    }
    
    /**
     * 移除机构成员
     *
     * @param userId 用户ID
     * @return 结果
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "移除机构成员", description = "从机构中移除指定用户")
    public Result<Void> removeMember(@PathVariable Long userId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        if (institutionId == null) {
            return Result.fail("用户未关联机构");
        }
        
        String username = SecurityUtil.getCurrentUsername();
        log.info("尝试移除机构成员: institutionId={}, userId={}, operator={}", institutionId, userId, username);
        
        try {
            memberService.removeMember(institutionId, userId, username);
            return Result.success();
        } catch (BusinessException e) {
            log.warn("移除机构成员失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("移除机构成员发生错误:", e);
            return Result.fail("操作失败，请稍后重试");
        }
    }
} 