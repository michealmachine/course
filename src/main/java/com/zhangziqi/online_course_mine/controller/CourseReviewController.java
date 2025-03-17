package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.review.ReviewCreateDTO;
import com.zhangziqi.online_course_mine.model.vo.CourseReviewSectionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.ReviewVO;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.CourseReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程评论控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
@Tag(name = "课程评论API", description = "课程评论相关接口")
public class CourseReviewController {
    
    private final CourseReviewService reviewService;
    
    /**
     * 获取课程评论区
     */
    @GetMapping("/{courseId}/reviews")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取课程评论区", description = "获取课程评论区，包含评分统计和评论列表")
    public Result<CourseReviewSectionVO> getCourseReviewSection(
            @PathVariable @Parameter(description = "课程ID") Long courseId,
            @RequestParam(required = false) @Parameter(description = "页码，从0开始") Integer page,
            @RequestParam(required = false) @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "排序方式: newest, highest_rating, lowest_rating") String orderBy) {
        
        log.info("获取课程评论区, 课程ID: {}, 页码: {}, 每页大小: {}, 排序方式: {}", courseId, page, size, orderBy);
        
        CourseReviewSectionVO reviewSection = reviewService.getCourseReviewSection(courseId, page, size, orderBy);
        return Result.success(reviewSection);
    }
    
    /**
     * 创建课程评论
     */
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建课程评论", description = "创建或更新课程评论")
    public Result<ReviewVO> createReview(@RequestBody @Valid ReviewCreateDTO reviewDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("创建课程评论, 课程ID: {}, 用户ID: {}, 评分: {}", 
                reviewDTO.getCourseId(), userId, reviewDTO.getRating());
        
        ReviewVO review = reviewService.createReview(reviewDTO, userId);
        return Result.success(review);
    }
    
    /**
     * 删除课程评论
     */
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除评论", description = "删除用户自己的评论")
    public Result<Void> deleteReview(@PathVariable @Parameter(description = "评论ID") Long reviewId) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("删除课程评论, 评论ID: {}, 用户ID: {}", reviewId, userId);
        
        reviewService.deleteReview(reviewId, userId);
        return Result.success();
    }
    
    /**
     * 获取当前用户对课程的评论
     */
    @GetMapping("/{courseId}/reviews/mine")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前用户对课程的评论", description = "获取当前用户对指定课程的评论信息")
    public Result<ReviewVO> getUserReviewOnCourse(@PathVariable @Parameter(description = "课程ID") Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("获取用户对课程的评论, 课程ID: {}, 用户ID: {}", courseId, userId);
        
        ReviewVO review = reviewService.getUserReviewOnCourse(userId, courseId);
        return Result.success(review);
    }
} 