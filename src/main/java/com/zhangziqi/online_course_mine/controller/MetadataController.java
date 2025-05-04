package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 元数据管理控制器
 * 用于提供标签和分类相关的扩展功能
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/metadata")
@Tag(name = "元数据管理", description = "提供标签和分类相关的扩展功能")
public class MetadataController {

    private final CourseService courseService;

    /**
     * 获取标签关联的所有课程（不限状态）
     *
     * @param tagId 标签ID
     * @param page 页码
     * @param size 每页大小
     * @return 课程分页
     */
    @GetMapping("/tags/{tagId}/courses")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "获取标签关联的所有课程", description = "获取标签关联的所有课程，不限课程状态")
    public Result<Page<CourseVO>> getCoursesByTagId(
            @Parameter(description = "标签ID") @PathVariable("tagId") Long tagId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取标签关联的所有课程: tagId={}, page={}, size={}", tagId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseVO> courses = courseService.getCoursesByTagId(tagId, pageable);
        
        return Result.success(courses);
    }

    /**
     * 获取分类关联的所有课程（不限状态）
     *
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页大小
     * @return 课程分页
     */
    @GetMapping("/categories/{categoryId}/courses")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "获取分类关联的所有课程", description = "获取分类关联的所有课程，不限课程状态")
    public Result<Page<CourseVO>> getCoursesByCategoryId(
            @Parameter(description = "分类ID") @PathVariable("categoryId") Long categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取分类关联的所有课程: categoryId={}, page={}, size={}", categoryId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CourseVO> courses = courseService.getCoursesByCategoryId(categoryId, pageable);
        
        return Result.success(courses);
    }
}
