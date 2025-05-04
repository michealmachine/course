package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionLearningStatisticsVO;
import com.zhangziqi.online_course_mine.model.vo.LearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningProgressTrendVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.AdminLearningStatisticsService;
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
 * 管理员学习统计控制器
 * 提供管理员查看全平台学习数据统计API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/learning-statistics")
@RequiredArgsConstructor
@Tag(name = "管理员学习统计", description = "提供管理员查看全平台学习数据统计相关接口")
public class AdminLearningStatisticsController {

    private final AdminLearningStatisticsService statisticsService;

    /**
     * 获取平台学习时长统计
     */
    @GetMapping("/duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取平台学习时长统计", description = "获取平台今日和总学习时长统计")
    public Result<Object> getLearningDuration() {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取平台学习时长统计, 用户名: {}", username);

        Long todayDuration = statisticsService.getTodayLearningDuration();
        Long totalDuration = statisticsService.getTotalLearningDuration();
        Long learnerCount = statisticsService.getTotalLearnerCount();

        // 构建响应
        return Result.success(new Object() {
            public final Long todayLearningDuration = todayDuration;
            public final Long totalLearningDuration = totalDuration;
            public final Long totalLearners = learnerCount;
        });
    }

    /**
     * 获取平台每日学习统计
     */
    @GetMapping("/daily")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取平台每日学习统计", description = "获取平台在指定日期范围内的每日学习统计数据")
    public Result<List<DailyLearningStatVO>> getDailyStatistics(
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取平台每日学习统计, 用户名: {}, 开始日期: {}, 结束日期: {}",
                username, startDate, endDate);

        List<DailyLearningStatVO> statistics =
                statisticsService.getAllDailyLearningStats(startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取平台活动类型统计
     */
    @GetMapping("/activity-types")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取平台活动类型统计", description = "获取平台各学习活动类型的统计数据")
    public Result<List<ActivityTypeStatVO>> getActivityTypeStatistics() {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取平台活动类型统计, 用户名: {}", username);

        List<ActivityTypeStatVO> statistics = statisticsService.getAllActivityTypeStats();
        return Result.success(statistics);
    }

    /**
     * 获取所有课程学习统计
     */
    @GetMapping("/courses")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有课程学习统计", description = "分页获取所有课程的学习统计数据")
    public Result<Page<InstitutionLearningStatisticsVO.CourseStatisticsVO>> getAllCourseStatistics(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取所有课程学习统计, 用户名: {}, 页码: {}, 每页数量: {}",
                username, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> statistics =
                statisticsService.getAllCourseStatistics(pageable);
        return Result.success(statistics);
    }

    /**
     * 获取机构学习统计概览
     */
    @GetMapping("/institutions/{institutionId}/overview")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取机构学习统计概览", description = "管理员获取指定机构的学习统计概览数据")
    public Result<InstitutionLearningStatisticsVO> getInstitutionStatisticsOverview(
            @Parameter(description = "机构ID") @PathVariable Long institutionId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构学习统计概览, 用户名: {}, 机构ID: {}", username, institutionId);

        InstitutionLearningStatisticsVO statistics = statisticsService.getInstitutionLearningStatistics(institutionId);
        return Result.success(statistics);
    }

    /**
     * 获取机构课程学习统计
     */
    @GetMapping("/institutions/{institutionId}/courses")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取机构课程学习统计", description = "分页获取特定机构下所有课程的学习统计数据")
    public Result<Page<InstitutionLearningStatisticsVO.CourseStatisticsVO>> getInstitutionCourseStatistics(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取机构课程学习统计, 用户名: {}, 机构ID: {}, 页码: {}, 每页数量: {}",
                username, institutionId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> statistics =
                statisticsService.getInstitutionCourseStatistics(institutionId, pageable);
        return Result.success(statistics);
    }

    /**
     * 获取课程学习统计概览
     */
    @GetMapping("/courses/{courseId}/overview")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取课程学习统计概览", description = "获取特定课程的学习统计概览数据")
    public Result<InstitutionLearningStatisticsVO.CourseStatisticsVO> getCourseStatisticsOverview(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取课程学习统计概览, 用户名: {}, 课程ID: {}",
                username, courseId);

        InstitutionLearningStatisticsVO.CourseStatisticsVO statistics =
                statisticsService.getCourseLearningStatistics(courseId);
        return Result.success(statistics);
    }

    /**
     * 获取课程每日学习统计
     */
    @GetMapping("/courses/{courseId}/daily")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取课程每日学习统计", description = "获取特定课程在指定日期范围内的每日学习统计数据")
    public Result<List<DailyLearningStatVO>> getCourseDailyStatistics(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取课程每日学习统计, 用户名: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                username, courseId, startDate, endDate);

        List<DailyLearningStatVO> statistics =
                statisticsService.getCourseDailyLearningStats(courseId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取课程活动类型统计
     */
    @GetMapping("/courses/{courseId}/activity-types")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取课程活动类型统计", description = "获取特定课程各学习活动类型的统计数据")
    public Result<List<ActivityTypeStatVO>> getCourseActivityTypeStatistics(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取课程活动类型统计, 用户名: {}, 课程ID: {}",
                username, courseId);

        List<ActivityTypeStatVO> statistics =
                statisticsService.getCourseActivityTypeStats(courseId);
        return Result.success(statistics);
    }

    /**
     * 获取课程学生学习统计
     */
    @GetMapping("/courses/{courseId}/students")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取课程学生学习统计", description = "分页获取特定课程的学生学习统计数据")
    public Result<Page<InstitutionLearningStatisticsVO.StudentLearningVO>> getCourseStudentStatistics(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取课程学生学习统计, 用户名: {}, 课程ID: {}, 页码: {}, 每页数量: {}",
                username, courseId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<InstitutionLearningStatisticsVO.StudentLearningVO> statistics =
                statisticsService.getCourseStudentStatistics(courseId, pageable);
        return Result.success(statistics);
    }

    /**
     * 获取课程学习热力图数据
     */
    @GetMapping("/courses/{courseId}/heatmap")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取课程学习热力图数据", description = "获取特定课程的学习活动热力图数据")
    public Result<LearningHeatmapVO> getCourseLearningHeatmap(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取课程学习热力图数据, 用户名: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                username, courseId, startDate, endDate);

        LearningHeatmapVO heatmap = statisticsService.getCourseLearningHeatmap(courseId, startDate, endDate);
        return Result.success(heatmap);
    }

    /**
     * 获取课程学习进度趋势
     */
    @GetMapping("/courses/{courseId}/progress-trend")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取课程学习进度趋势", description = "获取特定课程的学习进度趋势数据")
    public Result<LearningProgressTrendVO> getCourseLearningProgressTrend(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取课程学习进度趋势, 用户名: {}, 课程ID: {}, 开始日期: {}, 结束日期: {}",
                username, courseId, startDate, endDate);

        LearningProgressTrendVO trend = statisticsService.getCourseLearningProgressTrend(courseId, startDate, endDate);
        return Result.success(trend);
    }

    /**
     * 获取机构学习统计排行
     */
    @GetMapping("/institutions/ranking")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取机构学习统计排行", description = "获取机构学习统计排行数据，支持按不同指标排序")
    public Result<List<InstitutionLearningStatisticsVO.InstitutionStatisticsVO>> getInstitutionRanking(
            @Parameter(description = "排序字段(studentCount/courseCount/totalDuration/activityCount)")
            @RequestParam(defaultValue = "totalDuration") String sortBy,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取机构学习统计排行, 用户名: {}, 排序字段: {}, 数量限制: {}",
                username, sortBy, limit);

        List<InstitutionLearningStatisticsVO.InstitutionStatisticsVO> ranking =
                statisticsService.getInstitutionRanking(sortBy, limit);
        return Result.success(ranking);
    }

    /**
     * 获取课程学习统计排行
     */
    @GetMapping("/courses/ranking")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取课程学习统计排行", description = "获取课程学习统计排行数据，支持按不同指标排序")
    public Result<List<InstitutionLearningStatisticsVO.CourseStatisticsVO>> getCourseRanking(
            @Parameter(description = "排序字段(learnerCount/totalDuration/activityCount/favoriteCount)")
            @RequestParam(defaultValue = "totalDuration") String sortBy,
            @Parameter(description = "机构ID（可选）")
            @RequestParam(required = false) Long institutionId,
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取课程学习统计排行, 用户名: {}, 排序字段: {}, 机构ID: {}, 数量限制: {}",
                username, sortBy, institutionId, limit);

        List<InstitutionLearningStatisticsVO.CourseStatisticsVO> ranking =
                statisticsService.getCourseRanking(sortBy, institutionId, limit);
        return Result.success(ranking);
    }

    /**
     * 获取机构课程占比统计
     */
    @GetMapping("/institutions/course-distribution")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取机构课程占比统计", description = "获取各机构课程数量占比统计数据，用于饲图展示")
    public Result<List<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO>> getInstitutionCourseDistribution() {
        String username = SecurityUtil.getCurrentUsername();

        log.info("获取机构课程占比统计, 用户名: {}", username);

        List<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO> distribution =
                statisticsService.getInstitutionCourseDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取用户课程学习热力图数据
     */
    @GetMapping("/courses/{courseId}/users/{userId}/heatmap")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户课程学习热力图数据", description = "获取特定用户在特定课程的学习活动热力图数据")
    public Result<LearningHeatmapVO> getUserCourseLearningHeatmap(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取用户课程学习热力图数据, 用户名: {}, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                username, courseId, userId, startDate, endDate);

        LearningHeatmapVO heatmap = statisticsService.getUserCourseLearningHeatmap(courseId, userId, startDate, endDate);
        return Result.success(heatmap);
    }

    /**
     * 获取用户课程学习进度趋势
     */
    @GetMapping("/courses/{courseId}/users/{userId}/progress-trend")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户课程学习进度趋势", description = "获取特定用户在特定课程的学习进度趋势数据")
    public Result<LearningProgressTrendVO> getUserCourseLearningProgressTrend(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String username = SecurityUtil.getCurrentUsername();

        // 默认获取最近30天数据
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        log.info("获取用户课程学习进度趋势, 用户名: {}, 课程ID: {}, 用户ID: {}, 开始日期: {}, 结束日期: {}",
                username, courseId, userId, startDate, endDate);

        LearningProgressTrendVO trend = statisticsService.getUserCourseLearningProgressTrend(courseId, userId, startDate, endDate);
        return Result.success(trend);
    }

    /**
     * 清除统计缓存
     */
    @PostMapping("/cache/clear")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清除统计缓存", description = "清除所有统计数据的缓存")
    public Result<Void> clearStatisticsCache() {
        String username = SecurityUtil.getCurrentUsername();
        log.info("清除统计缓存, 用户名: {}", username);

        statisticsService.clearStatisticsCache();
        return Result.success();
    }
}
