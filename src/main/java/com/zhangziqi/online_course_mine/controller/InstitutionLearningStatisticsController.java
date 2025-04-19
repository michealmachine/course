package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionLearningStatisticsVO;
import com.zhangziqi.online_course_mine.model.vo.LearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningProgressTrendVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.InstitutionAuthService;
import com.zhangziqi.online_course_mine.service.InstitutionLearningStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 机构学习统计控制器
 * 提供机构学习数据统计API
 */
@Slf4j
@RestController
@RequestMapping("/api/institutions/learning-statistics")
@RequiredArgsConstructor
@Tag(name = "机构学习统计", description = "提供机构学习数据统计相关接口")
public class InstitutionLearningStatisticsController {

    private final InstitutionLearningStatisticsService statisticsService;
    private final InstitutionAuthService institutionAuthService;

    /**
     * 获取机构学习统计概览
     */
    @GetMapping("/overview")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取机构学习统计概览", description = "获取机构学习的综合统计数据")
    public Result<InstitutionLearningStatisticsVO> getStatisticsOverview() {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取机构学习统计概览, 用户名: {}, 机构ID: {}", username, institutionId);

        InstitutionLearningStatisticsVO statistics = statisticsService.getInstitutionLearningStatistics(institutionId);
        return Result.success(statistics);
    }

    /**
     * 获取机构每日学习统计
     */
    @GetMapping("/daily")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取机构每日学习统计", description = "获取机构在指定日期范围内的每日学习统计数据")
    public Result<List<DailyLearningStatVO>> getDailyStatistics(
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取机构每日学习统计, 用户名: {}, 机构ID: {}, 开始日期: {}, 结束日期: {}",
                username, institutionId, startDate, endDate);

        List<DailyLearningStatVO> statistics =
                statisticsService.getInstitutionDailyLearningStats(institutionId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取机构活动类型统计
     */
    @GetMapping("/activity-types")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取机构活动类型统计", description = "获取机构各学习活动类型的统计数据")
    public Result<List<ActivityTypeStatVO>> getActivityTypeStatistics() {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取机构活动类型统计, 用户名: {}, 机构ID: {}", username, institutionId);

        List<ActivityTypeStatVO> statistics =
                statisticsService.getInstitutionActivityTypeStats(institutionId);
        return Result.success(statistics);
    }

    /**
     * 获取机构课程学习统计
     */
    @GetMapping("/courses")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取机构课程学习统计", description = "分页获取机构下所有课程的学习统计数据")
    public Result<Page<InstitutionLearningStatisticsVO.CourseStatisticsVO>> getCourseStatistics(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取机构课程学习统计, 用户名: {}, 机构ID: {}, 页码: {}, 每页数量: {}",
                username, institutionId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> statistics =
                statisticsService.getInstitutionCourseStatistics(institutionId, pageable);
        return Result.success(statistics);
    }

    /**
     * 获取机构最活跃用户
     */
    @GetMapping("/active-users")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取机构最活跃用户", description = "获取机构学习时长最长的用户列表")
    public Result<List<InstitutionLearningStatisticsVO.ActiveUserVO>> getMostActiveUsers(
            @Parameter(description = "用户数量限制") @RequestParam(defaultValue = "10") int limit) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取机构最活跃用户, 用户名: {}, 机构ID: {}, 限制: {}", username, institutionId, limit);

        List<InstitutionLearningStatisticsVO.ActiveUserVO> users =
                statisticsService.getMostActiveUsers(institutionId, limit);
        return Result.success(users);
    }

    /**
     * 获取机构学习时长统计
     */
    @GetMapping("/duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取机构学习时长统计", description = "获取机构今日和总学习时长统计")
    public Result<Object> getLearningDuration() {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取机构学习时长统计, 用户名: {}, 机构ID: {}", username, institutionId);

        Long todayDuration = statisticsService.getInstitutionTodayLearningDuration(institutionId);
        Long totalDuration = statisticsService.getInstitutionTotalLearningDuration(institutionId);
        Long learnerCount = statisticsService.getInstitutionLearnerCount(institutionId);

        // 构建响应
        return Result.success(new Object() {
            public final Long todayLearningDuration = todayDuration;
            public final Long totalLearningDuration = totalDuration;
            public final Long totalLearners = learnerCount;
        });
    }

    /**
     * 获取课程学习统计概览
     */
    @GetMapping("/courses/{courseId}/overview")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取课程学习统计概览", description = "获取特定课程的学习统计概览数据")
    public Result<InstitutionLearningStatisticsVO.CourseStatisticsVO> getCourseStatisticsOverview(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取课程学习统计概览, 用户名: {}, 机构ID: {}, 课程ID: {}",
                username, institutionId, courseId);

        InstitutionLearningStatisticsVO.CourseStatisticsVO statistics =
                statisticsService.getCourseLearningStatistics(institutionId, courseId);
        return Result.success(statistics);
    }

    /**
     * 获取课程每日学习统计
     */
    @GetMapping("/courses/{courseId}/daily")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取课程每日学习统计", description = "获取特定课程在指定日期范围内的每日学习统计数据")
    public Result<List<DailyLearningStatVO>> getCourseDailyStatistics(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取课程每日学习统计, 用户名: {}, 机构ID: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                username, institutionId, courseId, startDate, endDate);

        List<DailyLearningStatVO> statistics =
                statisticsService.getCourseDailyLearningStats(institutionId, courseId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取课程活动类型统计
     */
    @GetMapping("/courses/{courseId}/activity-types")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取课程活动类型统计", description = "获取特定课程各学习活动类型的统计数据")
    public Result<List<ActivityTypeStatVO>> getCourseActivityTypeStatistics(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取课程活动类型统计, 用户名: {}, 机构ID: {}, 课程ID: {}",
                username, institutionId, courseId);

        List<ActivityTypeStatVO> statistics =
                statisticsService.getCourseActivityTypeStats(institutionId, courseId);
        return Result.success(statistics);
    }

    /**
     * 获取课程学生学习统计
     */
    @GetMapping("/courses/{courseId}/students")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取课程学生学习统计", description = "分页获取特定课程的学生学习统计数据")
    public Result<Page<InstitutionLearningStatisticsVO.StudentLearningVO>> getCourseStudentStatistics(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取课程学生学习统计, 用户名: {}, 机构ID: {}, 课程ID: {}, 页码: {}, 每页数量: {}",
                username, institutionId, courseId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<InstitutionLearningStatisticsVO.StudentLearningVO> statistics =
                statisticsService.getCourseStudentStatistics(institutionId, courseId, pageable);
        return Result.success(statistics);
    }

    /**
     * 获取课程学习时长统计
     */
    @GetMapping("/courses/{courseId}/duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取课程学习时长统计", description = "获取特定课程的今日和总学习时长统计")
    public Result<Object> getCourseLearningDuration(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取课程学习时长统计, 用户名: {}, 机构ID: {}, 课程ID: {}",
                username, institutionId, courseId);

        Long todayDuration = statisticsService.getCourseTodayLearningDuration(institutionId, courseId);
        Long totalDuration = statisticsService.getCourseTotalLearningDuration(institutionId, courseId);
        Long learnerCount = statisticsService.getCourseLearnerCount(institutionId, courseId);

        // 构建响应
        return Result.success(new Object() {
            public final Long todayLearningDuration = todayDuration;
            public final Long totalLearningDuration = totalDuration;
            public final Long totalLearners = learnerCount;
        });
    }

    /**
     * 获取课程学习热力图数据
     */
    @GetMapping("/courses/{courseId}/heatmap")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取课程学习热力图数据", description = "获取特定课程的学习活动热力图数据")
    public Result<LearningHeatmapVO> getCourseLearningHeatmap(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取课程学习热力图数据, 用户名: {}, 机构ID: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                username, institutionId, courseId, startDate, endDate);

        LearningHeatmapVO heatmap = statisticsService.getCourseLearningHeatmap(institutionId, courseId, startDate, endDate);
        return Result.success(heatmap);
    }

    /**
     * 获取课程学习进度趋势
     */
    @GetMapping("/courses/{courseId}/progress-trend")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取课程学习进度趋势", description = "获取特定课程的学习进度趋势数据")
    public Result<LearningProgressTrendVO> getCourseLearningProgressTrend(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取课程学习进度趋势, 用户名: {}, 机构ID: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                username, institutionId, courseId, startDate, endDate);

        LearningProgressTrendVO trend = statisticsService.getCourseLearningProgressTrend(institutionId, courseId, startDate, endDate);
        return Result.success(trend);
    }

    /**
     * 获取用户课程学习详情
     */
    @GetMapping("/courses/{courseId}/users/{userId}/detail")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取用户课程学习详情", description = "获取特定用户在特定课程的详细学习数据")
    public Result<InstitutionLearningStatisticsVO.StudentLearningDetailVO> getUserCourseLearningDetail(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取用户课程学习详情, 用户名: {}, 机构ID: {}, 课程ID: {}, 用户ID: {}",
                username, institutionId, courseId, userId);

        InstitutionLearningStatisticsVO.StudentLearningDetailVO detail =
                statisticsService.getUserCourseLearningDetail(institutionId, courseId, userId);
        return Result.success(detail);
    }

    /**
     * 获取用户课程学习热力图数据
     */
    @GetMapping("/courses/{courseId}/users/{userId}/heatmap")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取用户课程学习热力图数据", description = "获取特定用户在特定课程的学习活动热力图数据")
    public Result<LearningHeatmapVO> getUserCourseLearningHeatmap(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取用户课程学习热力图数据, 用户名: {}, 机构ID: {}, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                username, institutionId, courseId, userId, startDate, endDate);

        LearningHeatmapVO heatmap = statisticsService.getUserCourseLearningHeatmap(institutionId, courseId, userId, startDate, endDate);
        return Result.success(heatmap);
    }

    /**
     * 获取用户课程学习进度趋势
     */
    @GetMapping("/courses/{courseId}/users/{userId}/progress-trend")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取用户课程学习进度趋势", description = "获取特定用户在特定课程的学习进度趋势数据")
    public Result<LearningProgressTrendVO> getUserCourseLearningProgressTrend(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取用户课程学习进度趋势, 用户名: {}, 机构ID: {}, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                username, institutionId, courseId, userId, startDate, endDate);

        LearningProgressTrendVO trend = statisticsService.getUserCourseLearningProgressTrend(institutionId, courseId, userId, startDate, endDate);
        return Result.success(trend);
    }

    /**
     * 获取用户课程活动类型统计
     */
    @GetMapping("/courses/{courseId}/users/{userId}/activity-types")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('INSTITUTION')")
    @Operation(summary = "获取用户课程活动类型统计", description = "获取特定用户在特定课程的学习活动类型统计数据")
    public Result<List<ActivityTypeStatVO>> getUserCourseActivityTypeStats(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        String username = SecurityUtil.getCurrentUsername();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();

        log.info("获取用户课程活动类型统计, 用户名: {}, 机构ID: {}, 课程ID: {}, 用户ID: {}",
                username, institutionId, courseId, userId);

        List<ActivityTypeStatVO> stats = statisticsService.getUserCourseActivityTypeStats(institutionId, courseId, userId);
        return Result.success(stats);
    }
}