package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.enums.ReviewType;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.ReviewRecordVO;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.ReviewRecordService;
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

import java.util.List;

/**
 * 审核记录控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review-records")
@Tag(name = "审核记录", description = "审核记录相关接口")
public class ReviewRecordController {

    private final ReviewRecordService reviewRecordService;

    /**
     * 获取课程审核历史
     *
     * @param courseId 课程ID
     * @return 审核记录列表
     */
    @GetMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_REVIEWER', 'ROLE_INSTITUTION')")
    @Operation(summary = "获取课程审核历史", description = "获取指定课程的审核历史记录")
    public Result<List<ReviewRecordVO>> getCourseReviewHistory(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        log.info("获取课程审核历史: courseId={}", courseId);

        List<ReviewRecordVO> records = reviewRecordService.getCourseReviewHistory(courseId);

        return Result.success(records);
    }

    /**
     * 获取机构审核历史
     *
     * @param institutionId 机构ID
     * @return 审核记录列表
     */
    @GetMapping("/institutions/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_REVIEWER')")
    @Operation(summary = "获取机构审核历史", description = "获取指定机构的审核历史记录")
    public Result<List<ReviewRecordVO>> getInstitutionReviewHistory(
            @Parameter(description = "机构ID") @PathVariable Long institutionId) {
        log.info("获取机构审核历史: institutionId={}", institutionId);

        List<ReviewRecordVO> records = reviewRecordService.getInstitutionReviewHistory(institutionId);

        return Result.success(records);
    }

    /**
     * 获取审核员的审核记录
     * 如果是管理员，则返回所有审核记录
     * 如果是审核员，则只返回自己的审核记录
     *
     * @param reviewType 审核类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 审核记录分页
     */
    @GetMapping("/reviewer")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_REVIEWER')")
    @Operation(summary = "获取审核员的审核记录", description = "获取当前审核员的审核记录，管理员可查看所有记录")
    public Result<Page<ReviewRecordVO>> getReviewerRecords(
            @Parameter(description = "审核类型") @RequestParam(required = false) Integer reviewType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {

        boolean isAdmin = SecurityUtil.hasRole("ADMIN");
        Long reviewerId = null;

        // 如果不是管理员，则只能查看自己的记录
        if (!isAdmin) {
            reviewerId = SecurityUtil.getCurrentUserId();
        }

        log.info("获取审核员的审核记录: isAdmin={}, reviewerId={}, reviewType={}, page={}, size={}",
                isAdmin, reviewerId, reviewType, page, size);

        ReviewType type = reviewType != null ? ReviewType.getByValue(reviewType) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewedAt"));
        Page<ReviewRecordVO> records;

        if (isAdmin) {
            // 管理员查看所有记录，但可以按类型过滤
            records = reviewRecordService.getAllReviewRecords(type, pageable);
        } else {
            // 审核员只查看自己的记录，但可以按类型过滤
            records = reviewRecordService.getReviewerRecordsByType(reviewerId, type, pageable);
        }

        return Result.success(records);
    }

    /**
     * 获取所有审核记录（管理员使用）
     *
     * @param reviewType 审核类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 审核记录分页
     */
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "获取所有审核记录", description = "管理员获取所有审核记录")
    public Result<Page<ReviewRecordVO>> getAllReviewRecords(
            @Parameter(description = "审核类型") @RequestParam(required = false) Integer reviewType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("获取所有审核记录: reviewType={}, page={}, size={}", reviewType, page, size);

        ReviewType type = reviewType != null ? ReviewType.getByValue(reviewType) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewedAt"));
        Page<ReviewRecordVO> records = reviewRecordService.getAllReviewRecords(type, pageable);

        return Result.success(records);
    }

    /**
     * 获取机构相关的审核记录
     *
     * @param institutionId 机构ID
     * @param reviewType 审核类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 审核记录分页
     */
    @GetMapping("/institution/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_REVIEWER')")
    @Operation(summary = "获取机构相关的审核记录", description = "获取与指定机构相关的所有审核记录")
    public Result<Page<ReviewRecordVO>> getInstitutionReviewRecords(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "审核类型") @RequestParam(required = false) Integer reviewType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("获取机构相关的审核记录: institutionId={}, reviewType={}, page={}, size={}",
                institutionId, reviewType, page, size);

        ReviewType type = reviewType != null ? ReviewType.getByValue(reviewType) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reviewedAt"));
        Page<ReviewRecordVO> records = reviewRecordService.getInstitutionReviewRecords(institutionId, type, pageable);

        return Result.success(records);
    }
}
