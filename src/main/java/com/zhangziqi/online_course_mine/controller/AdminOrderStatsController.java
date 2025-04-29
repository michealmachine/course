package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.*;
import com.zhangziqi.online_course_mine.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员订单统计控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders/stats")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "管理员订单统计接口", description = "管理员订单统计相关接口")
public class AdminOrderStatsController {

    private final OrderService orderService;

    /**
     * 获取平台收入趋势
     */
    @GetMapping("/income-trend")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取平台收入趋势", description = "获取平台整体的收入趋势")
    public Result<List<IncomeTrendVO>> getPlatformIncomeTrend(
            @RequestParam(defaultValue = "30d") String timeRange,
            @RequestParam(defaultValue = "day") String groupBy) {
        log.info("获取平台收入趋势, 时间范围: {}, 分组方式: {}", timeRange, groupBy);
        
        List<IncomeTrendVO> trendData = orderService.getPlatformIncomeTrend(timeRange, groupBy);
        return Result.success(trendData);
    }
    
    /**
     * 获取平台订单状态分布
     */
    @GetMapping("/status-distribution")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取平台订单状态分布", description = "获取平台整体的订单状态分布")
    public Result<List<OrderStatusDistributionVO>> getPlatformOrderStatusDistribution() {
        log.info("获取平台订单状态分布");
        
        List<OrderStatusDistributionVO> distributionData = orderService.getPlatformOrderStatusDistribution();
        return Result.success(distributionData);
    }
    
    /**
     * 获取平台课程收入排行
     */
    @GetMapping("/course-income-ranking")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取平台课程收入排行", description = "获取平台整体的课程收入排行")
    public Result<List<AdminCourseIncomeRankingVO>> getPlatformCourseIncomeRanking(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("获取平台课程收入排行, 限制数量: {}", limit);
        
        List<AdminCourseIncomeRankingVO> rankingData = orderService.getPlatformCourseIncomeRanking(limit);
        return Result.success(rankingData);
    }
    
    /**
     * 获取平台收入统计
     */
    @GetMapping("/income-stats")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取平台收入统计", description = "获取平台整体的收入统计数据")
    public Result<PlatformIncomeStatsVO> getPlatformIncomeStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取平台收入统计, 开始时间: {}, 结束时间: {}", startDate, endDate);
        
        PlatformIncomeStatsVO statsData = orderService.getPlatformIncomeStats(startDate, endDate);
        return Result.success(statsData);
    }
}
