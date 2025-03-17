package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.course.CourseReviewDTO;
import com.zhangziqi.online_course_mine.model.vo.CourseStructureVO;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 审核员课程管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reviewer/courses")
@RequiredArgsConstructor
@Tag(name = "审核员课程管理", description = "审核员查看和审核课程相关操作")
public class ReviewerCourseController {
    
    private final CourseService courseService;
    
    /**
     * 获取待审核课程列表
     */
    @GetMapping("/pending")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_REVIEWER')")
    @Operation(summary = "获取待审核课程列表", description = "分页获取待审核的课程列表")
    public Result<Page<CourseVO>> getPendingCourses(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("获取待审核课程列表");
        
        Page<CourseVO> courses = courseService.getCoursesByStatus(
                1, pageable); // 1 表示待审核状态
        
        return Result.success(courses);
    }
    
    /**
     * 获取正在审核的课程列表
     */
    @GetMapping("/reviewing")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_REVIEWER')")
    @Operation(summary = "获取正在审核的课程列表", description = "分页获取当前审核员正在审核的课程列表")
    public Result<Page<CourseVO>> getReviewingCourses(
            @PageableDefault(size = 10) Pageable pageable) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        log.info("获取正在审核的课程列表, 审核员ID: {}", reviewerId);
        
        Page<CourseVO> courses = courseService.getCoursesByStatusAndReviewer(
                2, reviewerId, pageable); // 2 表示审核中状态
        
        return Result.success(courses);
    }
    
    /**
     * 获取课程结构详情
     */
    @GetMapping("/{id}/structure")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_REVIEWER')")
    @Operation(summary = "获取课程结构", description = "获取课程的完整结构，包括章节和小节")
    public Result<CourseStructureVO> getCourseStructure(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        log.info("审核员获取课程结构, 课程ID: {}", courseId);
        
        CourseStructureVO structure = courseService.getCourseStructure(courseId);
        
        return Result.success(structure);
    }
    
    /**
     * 开始审核课程
     */
    @PostMapping("/{id}/review/start")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_REVIEWER')")
    @Operation(summary = "开始审核课程", description = "审核员开始审核课程")
    public Result<CourseVO> startReview(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        log.info("开始审核课程, 课程ID: {}, 审核员ID: {}", courseId, reviewerId);
        
        CourseVO course = courseService.startReview(courseId, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 通过课程审核
     */
    @PostMapping("/{id}/review/approve")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_REVIEWER')")
    @Operation(summary = "通过课程审核", description = "审核员通过课程审核")
    public Result<CourseVO> approveCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "审核意见") @RequestParam(required = false) String comment) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        log.info("通过课程审核, 课程ID: {}, 审核员ID: {}", courseId, reviewerId);
        
        CourseVO course = courseService.approveCourse(courseId, comment, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 拒绝课程审核
     */
    @PostMapping("/{id}/review/reject")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_REVIEWER')")
    @Operation(summary = "拒绝课程审核", description = "审核员拒绝课程审核")
    public Result<CourseVO> rejectCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @RequestBody CourseReviewDTO dto) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        log.info("拒绝课程审核, 课程ID: {}, 审核员ID: {}, 原因: {}", 
                courseId, reviewerId, dto.getReason());
        
        CourseVO course = courseService.rejectCourse(courseId, dto.getReason(), reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_REVIEWER')")
    @Operation(summary = "获取课程详情", description = "审核员获取课程详情")
    public Result<CourseVO> getCourseById(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        log.info("审核员获取课程详情, 课程ID: {}", courseId);
        
        CourseVO course = courseService.getCourseById(courseId);
        
        return Result.success(course);
    }
} 