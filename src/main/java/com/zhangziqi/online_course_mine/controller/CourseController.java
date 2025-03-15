package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.course.CourseCreateDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.vo.PreviewUrlVO;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 课程控制器
 * 处理课程的创建、更新、查询和管理
 */
@Slf4j
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "课程管理", description = "课程创建、更新、查询和管理相关操作")
public class CourseController {
    
    private final CourseService courseService;
    
    /**
     * 创建课程
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "创建课程", description = "创建一个新的课程")
    public Result<Course> createCourse(@Valid @RequestBody CourseCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("创建课程, 用户ID: {}, 机构ID: {}, 课程标题: {}", 
                userId, institutionId, dto.getTitle());
        
        Course course = courseService.createCourse(dto, userId);
        
        return Result.success(course);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取课程详情", description = "获取指定课程的详细信息")
    public Result<Course> getCourseById(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取课程详情, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        Course course = courseService.getCourseById(courseId);
        
        return Result.success(course);
    }
    
    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新课程", description = "更新指定课程的信息")
    public Result<Course> updateCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Valid @RequestBody CourseCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新课程, 课程ID: {}, 机构ID: {}, 课程标题: {}", 
                courseId, institutionId, dto.getTitle());
        
        Course course = courseService.updateCourse(courseId, dto);
        
        return Result.success(course);
    }
    
    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除课程", description = "删除指定的课程")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除课程, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        courseService.deleteCourse(courseId);
        
        return Result.success();
    }
    
    /**
     * 获取机构课程列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取机构课程列表", description = "分页获取当前机构的课程列表")
    public Result<Page<Course>> getCoursesByInstitution(
            @PageableDefault(size = 10) Pageable pageable) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取机构课程列表, 机构ID: {}", institutionId);
        
        Page<Course> courses = courseService.getCoursesByInstitution(institutionId, pageable);
        
        return Result.success(courses);
    }
    
    /**
     * 提交课程审核
     */
    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "提交课程审核", description = "将课程提交审核")
    public Result<Course> submitForReview(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("提交课程审核, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        Course course = courseService.submitForReview(courseId);
        
        return Result.success(course);
    }
    
    /**
     * 更新课程封面
     */
    @PostMapping("/{id}/cover")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新课程封面", description = "更新指定课程的封面图片")
    public Result<Course> updateCourseCover(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "封面图片URL") @RequestParam String coverImageUrl) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新课程封面, 课程ID: {}, 机构ID: {}, 封面URL: {}", 
                courseId, institutionId, coverImageUrl);
        
        Course course = courseService.updateCourseCover(courseId, coverImageUrl);
        
        return Result.success(course);
    }
    
    /**
     * 更新课程支付设置
     */
    @PostMapping("/{id}/payment")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新课程支付设置", description = "更新指定课程的支付类型和价格")
    public Result<Course> updatePaymentSettings(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "支付类型") @RequestParam Integer paymentType,
            @Parameter(description = "价格") @RequestParam(required = false) BigDecimal price,
            @Parameter(description = "折扣价格") @RequestParam(required = false) BigDecimal discountPrice) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新课程支付设置, 课程ID: {}, 机构ID: {}, 支付类型: {}, 价格: {}, 折扣价格: {}", 
                courseId, institutionId, paymentType, price, discountPrice);
        
        Course course = courseService.updatePaymentSettings(courseId, paymentType, price, discountPrice);
        
        return Result.success(course);
    }
    
    /**
     * 生成课程预览URL
     */
    @GetMapping("/{id}/preview")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "生成课程预览URL", description = "生成一个临时的课程预览URL")
    public Result<PreviewUrlVO> generatePreviewUrl(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("生成课程预览URL, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        PreviewUrlVO previewUrl = courseService.generatePreviewUrl(courseId);
        
        return Result.success(previewUrl);
    }
    
    /**
     * 访问课程预览
     */
    @GetMapping("/preview/{token}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "访问课程预览", description = "通过预览令牌访问课程")
    public Result<Course> previewCourse(
            @Parameter(description = "预览令牌") @PathVariable("token") String token) {
        log.info("访问课程预览, 令牌: {}", token);
        
        Course course = courseService.getCourseByPreviewToken(token);
        
        return Result.success(course);
    }

    /**
     * 开始审核课程 (仅限管理员)
     */
    @PostMapping("/{id}/review/start")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "开始审核课程", description = "管理员开始审核课程")
    public Result<Course> startReview(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        
        log.info("开始审核课程, 课程ID: {}, 审核员ID: {}", courseId, reviewerId);
        
        Course course = courseService.startReview(courseId, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 通过课程审核 (仅限管理员)
     */
    @PostMapping("/{id}/review/approve")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "通过课程审核", description = "管理员通过课程审核")
    public Result<Course> approveCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "审核意见") @RequestParam(required = false) String comment) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        
        log.info("通过课程审核, 课程ID: {}, 审核员ID: {}, 审核意见: {}", courseId, reviewerId, comment);
        
        Course course = courseService.approveCourse(courseId, comment, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 拒绝课程审核 (仅限管理员)
     */
    @PostMapping("/{id}/review/reject")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "拒绝课程审核", description = "管理员拒绝课程审核")
    public Result<Course> rejectCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "拒绝原因") @RequestParam String reason) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        
        log.info("拒绝课程审核, 课程ID: {}, 审核员ID: {}, 拒绝原因: {}", courseId, reviewerId, reason);
        
        Course course = courseService.rejectCourse(courseId, reason, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 重新编辑被拒绝的课程
     */
    @PostMapping("/{id}/re-edit")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "重新编辑被拒绝的课程", description = "将被拒绝的课程重新变为草稿状态进行编辑")
    public Result<Course> reEditRejectedCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("重新编辑被拒绝的课程, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        Course course = courseService.reEditRejectedCourse(courseId);
        
        return Result.success(course);
    }
} 