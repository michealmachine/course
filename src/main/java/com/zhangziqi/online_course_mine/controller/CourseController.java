package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.course.CourseCreateDTO;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.CourseStructureVO;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public Result<CourseVO> createCourse(@Valid @RequestBody CourseCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("创建课程, 用户ID: {}, 机构ID: {}, 课程标题: {}", 
                userId, institutionId, dto.getTitle());
        
        CourseVO course = courseService.createCourse(dto, userId, institutionId);
        
        return Result.success(course);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取课程详情", description = "获取指定课程的详细信息")
    public Result<CourseVO> getCourseById(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取课程详情, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        CourseVO course = courseService.getCourseById(courseId);
        
        return Result.success(course);
    }
    
    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新课程", description = "更新指定课程的信息")
    public Result<CourseVO> updateCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Valid @RequestBody CourseCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新课程, 课程ID: {}, 机构ID: {}, 课程标题: {}", 
                courseId, institutionId, dto.getTitle());
        
        CourseVO course = courseService.updateCourse(courseId, dto, institutionId);
        
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
    @Operation(summary = "获取机构工作区课程列表", description = "分页获取当前机构的工作区课程列表（非发布版本）")
    public Result<Page<CourseVO>> getCoursesByInstitution(
            @PageableDefault(size = 10) Pageable pageable) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取机构工作区课程列表, 机构ID: {}", institutionId);
        
        Page<CourseVO> courses = courseService.getWorkspaceCoursesByInstitution(institutionId, pageable);
        
        return Result.success(courses);
    }
    
    /**
     * 获取机构发布版本课程列表
     */
    @GetMapping("/published")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取机构发布版本课程列表", description = "分页获取当前机构的发布版本课程列表")
    public Result<Page<CourseVO>> getPublishedCoursesByInstitution(
            @PageableDefault(size = 10) Pageable pageable) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取机构发布版本课程列表, 机构ID: {}", institutionId);
        
        Page<CourseVO> courses = courseService.getPublishedCoursesByInstitution(institutionId, pageable);
        
        return Result.success(courses);
    }
    
    /**
     * 获取课程的发布版本
     */
    @GetMapping("/{id}/published-version")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取课程的发布版本", description = "根据工作区版本ID获取对应的发布版本")
    public Result<CourseVO> getPublishedVersion(
            @Parameter(description = "工作区版本课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取课程发布版本, 工作区课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        CourseVO publishedVersion = courseService.getPublishedVersionByWorkspaceId(courseId);
        
        if (publishedVersion == null) {
            return Result.fail(404, "该课程尚未发布");
        }
        
        return Result.success(publishedVersion);
    }
    
    /**
     * 提交课程审核
     */
    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "提交课程审核", description = "将课程提交审核")
    public Result<CourseVO> submitForReview(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("提交课程审核, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        CourseVO course = courseService.submitForReview(courseId);
        
        return Result.success(course);
    }
    
    /**
     * 更新课程封面
     */
    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新课程封面", description = "上传课程封面图片")
    public Result<CourseVO> updateCourseCover(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "封面图片文件") @RequestParam("file") MultipartFile file) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新课程封面, 课程ID: {}, 机构ID: {}, 文件大小: {}", 
                courseId, institutionId, file.getSize());
        
        try {
            CourseVO course = courseService.updateCourseCover(courseId, file);
            return Result.success(course);
        } catch (BusinessException e) {
            return Result.fail(e.getCode(), e.getMessage());
        } catch (IOException e) {
            log.error("课程封面上传失败", e);
            return Result.fail(500, "课程封面上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新课程支付设置
     */
    @PostMapping("/{id}/payment")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新课程支付设置", description = "更新指定课程的支付类型和价格")
    public Result<CourseVO> updatePaymentSettings(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "支付类型") @RequestParam Integer paymentType,
            @Parameter(description = "价格") @RequestParam(required = false) BigDecimal price,
            @Parameter(description = "折扣价格") @RequestParam(required = false) BigDecimal discountPrice) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新课程支付设置, 课程ID: {}, 机构ID: {}, 支付类型: {}, 价格: {}, 折扣价格: {}", 
                courseId, institutionId, paymentType, price, discountPrice);
        
        CourseVO course = courseService.updatePaymentSettings(courseId, paymentType, price, discountPrice);
        
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
    @Operation(summary = "访问课程预览", description = "通过预览令牌访问课程结构")
    public Result<CourseStructureVO> previewCourse(
            @Parameter(description = "预览令牌") @PathVariable("token") String token) {
        log.info("访问课程预览, 令牌: {}", token);
        
        CourseStructureVO courseStructure = courseService.getCourseStructureByPreviewToken(token);
        
        return Result.success(courseStructure);
    }

    /**
     * 开始审核课程 (仅限管理员)
     */
    @PostMapping("/{id}/review/start")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_REVIEWER')")
    @Operation(summary = "开始审核课程", description = "管理员或审核员开始审核课程")
    public Result<CourseVO> startReview(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        
        log.info("开始审核课程, 课程ID: {}, 审核员ID: {}", courseId, reviewerId);
        
        CourseVO course = courseService.startReview(courseId, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 通过课程审核 (仅限管理员)
     */
    @PostMapping("/{id}/review/approve")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_REVIEWER')")
    @Operation(summary = "通过课程审核", description = "管理员或审核员通过课程审核")
    public Result<CourseVO> approveCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "审核意见") @RequestParam(required = false) String comment) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        
        log.info("通过课程审核, 课程ID: {}, 审核员ID: {}", courseId, reviewerId);
        
        CourseVO course = courseService.approveCourse(courseId, comment, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 拒绝课程审核 (仅限管理员)
     */
    @PostMapping("/{id}/review/reject")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_REVIEWER')")
    @Operation(summary = "拒绝课程审核", description = "管理员或审核员拒绝课程审核")
    public Result<CourseVO> rejectCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId,
            @Parameter(description = "拒绝原因") @RequestParam String reason) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        
        log.info("拒绝课程审核, 课程ID: {}, 审核员ID: {}", courseId, reviewerId);
        
        CourseVO course = courseService.rejectCourse(courseId, reason, reviewerId);
        
        return Result.success(course);
    }
    
    /**
     * 重新编辑被拒绝的课程
     */
    @PostMapping("/{id}/re-edit")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "重新编辑被拒绝的课程", description = "将被拒绝的课程重新变为草稿状态进行编辑")
    public Result<CourseVO> reEditRejectedCourse(
            @Parameter(description = "课程ID") @PathVariable("id") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("重新编辑被拒绝的课程, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        CourseVO course = courseService.reEditRejectedCourse(courseId);
        
        return Result.success(course);
    }
} 