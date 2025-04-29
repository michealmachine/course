package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.CourseIncomeRankingVO;
import com.zhangziqi.online_course_mine.model.vo.IncomeTrendVO;
import com.zhangziqi.online_course_mine.model.vo.OrderStatusDistributionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单统计控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/stats")
@Tag(name = "订单统计接口", description = "订单统计相关接口")
public class OrderStatsController {

    private final OrderService orderService;

    /**
     * 获取机构收入趋势
     */
    @GetMapping("/institution/income-trend")
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构收入趋势", description = "获取当前用户所属机构的收入趋势")
    public Result<List<IncomeTrendVO>> getInstitutionIncomeTrend(
            @RequestParam(defaultValue = "30d") String timeRange,
            @RequestParam(defaultValue = "day") String groupBy) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        log.info("获取机构收入趋势, 机构ID: {}, 时间范围: {}, 分组方式: {}", institutionId, timeRange, groupBy);
        
        List<IncomeTrendVO> trendData = orderService.getInstitutionIncomeTrend(institutionId, timeRange, groupBy);
        return Result.success(trendData);
    }
    
    /**
     * 获取机构订单状态分布
     */
    @GetMapping("/institution/status-distribution")
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构订单状态分布", description = "获取当前用户所属机构的订单状态分布")
    public Result<List<OrderStatusDistributionVO>> getInstitutionOrderStatusDistribution() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        log.info("获取机构订单状态分布, 机构ID: {}", institutionId);
        
        List<OrderStatusDistributionVO> distributionData = orderService.getInstitutionOrderStatusDistribution(institutionId);
        return Result.success(distributionData);
    }
    
    /**
     * 获取机构课程收入排行
     */
    @GetMapping("/institution/course-income-ranking")
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构课程收入排行", description = "获取当前用户所属机构的课程收入排行")
    public Result<List<CourseIncomeRankingVO>> getInstitutionCourseIncomeRanking(
            @RequestParam(defaultValue = "10") int limit) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        log.info("获取机构课程收入排行, 机构ID: {}, 限制数量: {}", institutionId, limit);
        
        List<CourseIncomeRankingVO> rankingData = orderService.getInstitutionCourseIncomeRanking(institutionId, limit);
        return Result.success(rankingData);
    }
}
