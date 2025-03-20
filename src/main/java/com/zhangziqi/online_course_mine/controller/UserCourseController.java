package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.LearningProgressUpdateDTO;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.UserCourseVO;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.UserCourseService;
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

import java.util.List;

/**
 * 用户课程控制器
 * 处理用户已购课程的查询和学习记录等操作
 */
@Slf4j
@RestController
@RequestMapping("/api/user-courses")
@RequiredArgsConstructor
@Tag(name = "用户课程", description = "用户课程查询、学习记录等相关操作")
public class UserCourseController {
    
    private final UserCourseService userCourseService;
    
    /**
     * 获取当前用户的已购课程列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取已购课程", description = "获取当前用户的已购课程列表")
    public Result<List<UserCourseVO>> getUserPurchasedCourses() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户已购课程列表, 用户ID: {}", userId);
        
        List<UserCourseVO> courses = userCourseService.getUserPurchasedCourses(userId);
        return Result.success(courses);
    }
    
    /**
     * 分页获取当前用户的已购课程列表
     */
    @GetMapping("/page")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "分页获取已购课程", description = "分页获取当前用户的已购课程列表")
    public Result<Page<UserCourseVO>> getUserPurchasedCoursesWithPagination(
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("分页获取用户已购课程列表, 用户ID: {}", userId);
        
        Page<UserCourseVO> coursePage = userCourseService.getUserPurchasedCourses(userId, pageable);
        return Result.success(coursePage);
    }
    
    /**
     * 获取当前用户的课程学习记录
     */
    @GetMapping("/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取课程学习记录", description = "获取当前用户的指定课程学习记录")
    public Result<UserCourseVO> getUserCourseRecord(
            @Parameter(description = "课程ID") @PathVariable Long courseId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户课程学习记录, 用户ID: {}, 课程ID: {}", userId, courseId);
        
        UserCourseVO userCourseVO = userCourseService.getUserCourseRecord(userId, courseId);
        return Result.success(userCourseVO);
    }
    
    /**
     * 分页获取当前用户的有效课程（状态正常的课程）
     */
    @GetMapping("/valid")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "分页获取有效课程", description = "分页获取当前用户状态正常的课程列表")
    public Result<Page<CourseVO>> getUserValidCoursesWithPagination(
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("分页获取用户有效课程列表, 用户ID: {}, 分页参数: {}", userId, pageable);
        
        Page<UserCourse> userCoursePage = userCourseService.findByUserIdAndStatus(
                userId, UserCourseStatus.NORMAL.getValue(), pageable);
        
        Page<CourseVO> coursePage = userCoursePage.map(userCourse -> 
                CourseVO.fromEntity(userCourse.getCourse()));
        
        return Result.success(coursePage);
    }
    
    /**
     * 更新用户的课程学习进度
     */
    @PutMapping("/{courseId}/progress")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新学习进度", description = "更新当前用户的指定课程学习进度")
    public Result<UserCourseVO> updateLearningProgress(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学习进度更新信息") @RequestBody LearningProgressUpdateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("更新用户课程学习进度, 用户ID: {}, 课程ID: {}, 章节ID: {}, 小节ID: {}, 进度: {}%", 
                userId, courseId, dto.getChapterId(), dto.getSectionId(), dto.getSectionProgress());
        
        UserCourseVO userCourseVO = userCourseService.updateLearningProgress(userId, courseId, dto);
        return Result.success(userCourseVO);
    }
    
    /**
     * 记录用户的学习时长
     */
    @PutMapping("/{courseId}/duration")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "记录学习时长", description = "记录当前用户的指定课程学习时长")
    public Result<UserCourseVO> recordLearningDuration(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "学习时长(秒)") @RequestParam Integer duration) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("记录用户课程学习时长, 用户ID: {}, 课程ID: {}, 时长: {}秒", userId, courseId, duration);
        
        UserCourseVO userCourseVO = userCourseService.recordLearningDuration(userId, courseId, duration);
        return Result.success(userCourseVO);
    }
    
    /**
     * 获取用户最近学习的课程
     */
    @GetMapping("/recent")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取最近学习课程", description = "获取当前用户最近学习的课程列表")
    public Result<List<CourseVO>> getRecentLearnedCourses(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "5") Integer limit) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户最近学习课程, 用户ID: {}, 限制数量: {}", userId, limit);
        
        List<CourseVO> courses = userCourseService.getRecentLearnedCourses(userId, limit);
        return Result.success(courses);
    }
} 