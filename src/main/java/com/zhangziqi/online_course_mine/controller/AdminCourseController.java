package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.course.CourseSearchDTO;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员课程控制器
 * 提供管理员查看和管理课程的API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@Tag(name = "管理员课程管理", description = "提供管理员查看和管理课程的相关接口")
public class AdminCourseController {

    private final CourseService courseService;

    /**
     * 获取机构的已发布课程列表
     */
    @GetMapping("/institutions/{institutionId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取机构已发布课程列表", description = "管理员分页获取指定机构的已发布课程列表")
    public Result<Page<CourseVO>> getInstitutionPublishedCourses(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构已发布课程列表, 用户名: {}, 机构ID: {}, 页码: {}, 每页数量: {}",
                username, institutionId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseVO> courses = courseService.getPublishedCoursesByInstitution(institutionId, pageable);

        return Result.success(courses);
    }

    /**
     * 获取机构的工作区课程列表
     */
    @GetMapping("/institutions/{institutionId}/workspace")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取机构工作区课程列表", description = "管理员分页获取指定机构的工作区课程列表")
    public Result<Page<CourseVO>> getInstitutionWorkspaceCourses(
            @Parameter(description = "机构ID") @PathVariable Long institutionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取机构工作区课程列表, 用户名: {}, 机构ID: {}, 页码: {}, 每页数量: {}",
                username, institutionId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseVO> courses = courseService.getWorkspaceCoursesByInstitution(institutionId, pageable);

        return Result.success(courses);
    }

    /**
     * 搜索机构课程
     */
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "搜索课程", description = "管理员根据多种条件搜索课程")
    public Result<Page<CourseVO>> searchCourses(
            @Valid @RequestBody CourseSearchDTO searchDTO) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员搜索课程, 用户名: {}, 关键词: {}, 机构ID: {}, 页码: {}, 每页大小: {}",
                username, searchDTO.getKeyword(), searchDTO.getInstitutionId(),
                searchDTO.getPage(), searchDTO.getPageSize());

        // 使用DTO中的page和pageSize创建Pageable对象
        // 注意：页码从0开始计数，而前端通常从1开始，需要转换
        int page = searchDTO.getPage() != null ? Math.max(0, searchDTO.getPage() - 1) : 0;
        int size = searchDTO.getPageSize() != null ? searchDTO.getPageSize() : 10;

        // 创建分页请求对象
        Pageable pageable = PageRequest.of(page, size);

        // 调用服务层方法进行搜索
        Page<CourseVO> courses = courseService.searchCourses(searchDTO, pageable);

        return Result.success(courses);
    }

    /**
     * 获取热门课程
     */
    @GetMapping("/hot")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取热门课程", description = "管理员获取热门课程列表")
    public Result<List<CourseVO>> getHotCourses(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取热门课程, 用户名: {}, 数量限制: {}", username, limit);

        List<CourseVO> courses = courseService.getHotCourses(limit);

        return Result.success(courses);
    }

    /**
     * 获取最新课程
     */
    @GetMapping("/latest")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取最新课程", description = "管理员获取最新课程列表")
    public Result<List<CourseVO>> getLatestCourses(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取最新课程, 用户名: {}, 数量限制: {}", username, limit);

        List<CourseVO> courses = courseService.getLatestCourses(limit);

        return Result.success(courses);
    }

    /**
     * 获取高评分课程
     */
    @GetMapping("/top-rated")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取高评分课程", description = "管理员获取高评分课程列表")
    public Result<List<CourseVO>> getTopRatedCourses(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取高评分课程, 用户名: {}, 数量限制: {}", username, limit);

        List<CourseVO> courses = courseService.getTopRatedCourses(limit);

        return Result.success(courses);
    }

    /**
     * 获取所有课程（分页）
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有课程", description = "管理员分页获取所有课程")
    public Result<Page<CourseVO>> getAllCourses(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        String username = SecurityUtil.getCurrentUsername();

        log.info("管理员获取所有课程, 用户名: {}, 页码: {}, 每页数量: {}",
                username, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseVO> courses = courseService.getAllCourses(pageable);

        return Result.success(courses);
    }
}
