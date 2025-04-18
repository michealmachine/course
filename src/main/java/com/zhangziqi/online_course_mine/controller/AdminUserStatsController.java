package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.UserActivityStatsVO;
import com.zhangziqi.online_course_mine.model.vo.UserGrowthStatsVO;
import com.zhangziqi.online_course_mine.model.vo.UserRoleDistributionVO;
import com.zhangziqi.online_course_mine.model.vo.UserStatsVO;
import com.zhangziqi.online_course_mine.model.vo.UserStatusStatsVO;
import com.zhangziqi.online_course_mine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员用户统计控制器
 * 用于管理员查看用户统计信息
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user-stats")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员用户统计", description = "管理员查看用户统计信息相关接口")
public class AdminUserStatsController {

    private final UserService userService;
    
    /**
     * 获取用户统计综合数据
     *
     * @return 用户统计综合数据
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取用户统计综合数据", description = "获取包含角色分布、增长、状态、活跃度的综合统计数据")
    public Result<UserStatsVO> getUserStats() {
        log.info("管理员请求用户统计综合数据");
        UserStatsVO stats = userService.getUserStats();
        return Result.success(stats);
    }
    
    /**
     * 获取用户角色分布统计
     *
     * @return 用户角色分布统计
     */
    @GetMapping("/role-distribution")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取用户角色分布统计", description = "获取系统中各角色的用户分布情况")
    public Result<UserRoleDistributionVO> getUserRoleDistribution() {
        log.info("管理员请求用户角色分布统计");
        UserRoleDistributionVO stats = userService.getUserRoleDistribution();
        return Result.success(stats);
    }
    
    /**
     * 获取用户增长统计
     *
     * @return 用户增长统计
     */
    @GetMapping("/growth")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取用户增长统计", description = "获取用户增长趋势和数据")
    public Result<UserGrowthStatsVO> getUserGrowthStats() {
        log.info("管理员请求用户增长统计");
        UserGrowthStatsVO stats = userService.getUserGrowthStats();
        return Result.success(stats);
    }
    
    /**
     * 获取用户状态统计
     *
     * @return 用户状态统计
     */
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取用户状态统计", description = "获取用户状态(启用/禁用)分布统计")
    public Result<UserStatusStatsVO> getUserStatusStats() {
        log.info("管理员请求用户状态统计");
        UserStatusStatsVO stats = userService.getUserStatusStats();
        return Result.success(stats);
    }
    
    /**
     * 获取用户活跃度统计
     *
     * @return 用户活跃度统计
     */
    @GetMapping("/activity")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取用户活跃度统计", description = "获取用户活跃度和登录分布统计")
    public Result<UserActivityStatsVO> getUserActivityStats() {
        log.info("管理员请求用户活跃度统计");
        UserActivityStatsVO stats = userService.getUserActivityStats();
        return Result.success(stats);
    }
} 