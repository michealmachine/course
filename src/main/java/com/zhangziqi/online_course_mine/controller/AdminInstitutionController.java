package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.InstitutionQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.CourseIncomeRankingVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.IncomeTrendVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionLearningStatisticsVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionVO;
import com.zhangziqi.online_course_mine.model.vo.OrderStatusDistributionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.InstitutionLearningStatisticsService;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import com.zhangziqi.online_course_mine.service.OrderService;
import com.zhangziqi.online_course_mine.service.UserService;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员机构管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/institutions")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员机构管理", description = "管理员机构管理相关接口")
public class AdminInstitutionController {

    private final InstitutionService institutionService;
    private final UserService userService;
    private final InstitutionLearningStatisticsService learningStatisticsService;
    private final OrderService orderService;

    /**
     * 获取机构列表
     *
     * @param queryDTO 查询参数
     * @return 机构分页
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构列表", description = "管理员分页获取机构列表，支持按名称、状态等条件筛选")
    public Result<Page<InstitutionVO>> getInstitutions(InstitutionQueryDTO queryDTO) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构列表: username={}, query={}", username, queryDTO);

        // 创建分页和排序参数
        Pageable pageable = PageRequest.of(
                queryDTO.getPage(),
                queryDTO.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 调用服务获取机构列表
        Page<InstitutionVO> institutions = institutionService.getInstitutions(queryDTO, pageable);

        return Result.success(institutions);
    }

    /**
     * 获取机构详情
     *
     * @param institutionId 机构ID
     * @return 机构详情
     */
    @GetMapping("/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构详情", description = "管理员获取机构详情，包括注册码")
    public Result<InstitutionVO> getInstitutionDetail(
            @Parameter(description = "机构ID") @PathVariable Long institutionId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构详情: username={}, institutionId={}", username, institutionId);

        // 调用服务获取机构详情（管理员可以看到注册码）
        InstitutionVO institution = institutionService.getAdminInstitutionDetail(institutionId);

        return Result.success(institution);
    }

    /**
     * 获取机构用户列表
     *
     * @param institutionId 机构ID
     * @param keyword 关键词（用户名或邮箱）
     * @param page 页码
     * @param size 每页大小
     * @return 用户分页
     */
    @GetMapping("/{institutionId}/users")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构用户列表", description = "管理员分页获取机构用户列表，支持按用户名或邮箱搜索")
    public Result<Page<UserVO>> getInstitutionUsers(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "关键词（用户名或邮箱）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构用户列表: username={}, institutionId={}, keyword={}, page={}, size={}",
                username, institutionId, keyword, page, size);

        // 创建分页参数
        Pageable pageable = PageRequest.of(page, size);

        // 调用服务获取机构用户列表
        Page<UserVO> users = institutionService.getInstitutionUsers(institutionId, keyword, pageable);

        return Result.success(users);
    }

    /**
     * 获取机构统计数据
     *
     * @param institutionId 机构ID
     * @return 机构统计数据
     */
    @GetMapping("/{institutionId}/stats")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构统计数据", description = "管理员获取机构统计数据，包括用户数、课程数、学习时长等")
    public Result<InstitutionVO.InstitutionStatsVO> getInstitutionStats(
            @Parameter(description = "机构ID") @PathVariable Long institutionId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构统计数据: username={}, institutionId={}", username, institutionId);

        // 调用服务获取机构统计数据
        InstitutionVO.InstitutionStatsVO stats = institutionService.getInstitutionStats(institutionId);

        return Result.success(stats);
    }

    /**
     * 获取机构学习统计概览
     *
     * @param institutionId 机构ID
     * @return 机构学习统计概览
     */
    @GetMapping("/{institutionId}/learning-statistics/overview")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构学习统计概览", description = "管理员获取机构学习的综合统计数据")
    public Result<InstitutionLearningStatisticsVO> getInstitutionLearningStatisticsOverview(
            @Parameter(description = "机构ID") @PathVariable Long institutionId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构学习统计概览: username={}, institutionId={}", username, institutionId);

        // 调用服务获取机构学习统计概览
        InstitutionLearningStatisticsVO statistics = learningStatisticsService.getInstitutionLearningStatistics(institutionId);

        return Result.success(statistics);
    }

    /**
     * 获取机构每日学习统计
     *
     * @param institutionId 机构ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日学习统计列表
     */
    @GetMapping("/{institutionId}/learning-statistics/daily")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构每日学习统计", description = "管理员获取机构每日学习统计数据")
    public Result<List<DailyLearningStatVO>> getInstitutionDailyLearningStats(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();

        // 默认查询最近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("管理员获取机构每日学习统计: username={}, institutionId={}, startDate={}, endDate={}",
                username, institutionId, startDate, endDate);

        // 调用服务获取机构每日学习统计
        List<DailyLearningStatVO> statistics =
                learningStatisticsService.getInstitutionDailyLearningStats(institutionId, startDate, endDate);

        return Result.success(statistics);
    }

    /**
     * 获取机构活动类型统计
     *
     * @param institutionId 机构ID
     * @return 活动类型统计列表
     */
    @GetMapping("/{institutionId}/learning-statistics/activity-types")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构活动类型统计", description = "管理员获取机构各学习活动类型的统计数据")
    public Result<List<ActivityTypeStatVO>> getInstitutionActivityTypeStats(
            @Parameter(description = "机构ID") @PathVariable Long institutionId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构活动类型统计: username={}, institutionId={}", username, institutionId);

        // 调用服务获取机构活动类型统计
        List<ActivityTypeStatVO> statistics =
                learningStatisticsService.getInstitutionActivityTypeStats(institutionId);

        return Result.success(statistics);
    }

    /**
     * 获取机构收入趋势
     *
     * @param institutionId 机构ID
     * @param timeRange 时间范围
     * @param groupBy 分组方式
     * @return 收入趋势数据列表
     */
    @GetMapping("/{institutionId}/income-trend")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构收入趋势", description = "管理员获取机构的收入趋势")
    public Result<List<IncomeTrendVO>> getInstitutionIncomeTrend(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "30d") String timeRange,
            @Parameter(description = "分组方式") @RequestParam(defaultValue = "day") String groupBy) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构收入趋势: username={}, institutionId={}, timeRange={}, groupBy={}",
                username, institutionId, timeRange, groupBy);

        // 调用服务获取机构收入趋势
        List<IncomeTrendVO> trendData = orderService.getInstitutionIncomeTrend(institutionId, timeRange, groupBy);

        return Result.success(trendData);
    }

    /**
     * 获取机构订单状态分布
     *
     * @param institutionId 机构ID
     * @return 订单状态分布数据列表
     */
    @GetMapping("/{institutionId}/order-status-distribution")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构订单状态分布", description = "管理员获取机构的订单状态分布")
    public Result<List<OrderStatusDistributionVO>> getInstitutionOrderStatusDistribution(
            @Parameter(description = "机构ID") @PathVariable Long institutionId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构订单状态分布: username={}, institutionId={}", username, institutionId);

        // 调用服务获取机构订单状态分布
        List<OrderStatusDistributionVO> distributionData =
                orderService.getInstitutionOrderStatusDistribution(institutionId);

        return Result.success(distributionData);
    }

    /**
     * 获取机构课程收入排行
     *
     * @param institutionId 机构ID
     * @param limit 返回数量限制
     * @return 课程收入排行数据列表
     */
    @GetMapping("/{institutionId}/course-income-ranking")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构课程收入排行", description = "管理员获取机构的课程收入排行")
    public Result<List<CourseIncomeRankingVO>> getInstitutionCourseIncomeRanking(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "5") int limit) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构课程收入排行: username={}, institutionId={}, limit={}",
                username, institutionId, limit);

        // 调用服务获取机构课程收入排行
        List<CourseIncomeRankingVO> rankingData =
                orderService.getInstitutionCourseIncomeRanking(institutionId, limit);

        return Result.success(rankingData);
    }
}
