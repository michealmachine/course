package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.chapter.ChapterCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.chapter.ChapterOrderDTO;
import com.zhangziqi.online_course_mine.model.vo.ChapterVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节控制器
 * 处理课程章节的创建、更新、查询和管理
 */
@Slf4j
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Tag(name = "章节管理", description = "课程章节创建、更新、查询和管理相关操作")
public class ChapterController {
    
    private final ChapterService chapterService;
    
    /**
     * 创建章节
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "创建章节", description = "创建一个新的课程章节")
    public Result<ChapterVO> createChapter(@Valid @RequestBody ChapterCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("创建章节, 课程ID: {}, 机构ID: {}, 章节标题: {}", 
                dto.getCourseId(), institutionId, dto.getTitle());
        
        ChapterVO chapter = chapterService.createChapter(dto);
        
        return Result.success(chapter);
    }
    
    /**
     * 获取章节详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取章节详情", description = "获取指定章节的详细信息")
    public Result<ChapterVO> getChapterById(
            @Parameter(description = "章节ID") @PathVariable("id") Long chapterId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取章节详情, 章节ID: {}, 机构ID: {}", chapterId, institutionId);
        
        ChapterVO chapter = chapterService.getChapterById(chapterId);
        
        return Result.success(chapter);
    }
    
    /**
     * 更新章节
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新章节", description = "更新指定章节的信息")
    public Result<ChapterVO> updateChapter(
            @Parameter(description = "章节ID") @PathVariable("id") Long chapterId,
            @Valid @RequestBody ChapterCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新章节, 章节ID: {}, 机构ID: {}, 章节标题: {}", 
                chapterId, institutionId, dto.getTitle());
        
        ChapterVO chapter = chapterService.updateChapter(chapterId, dto);
        
        return Result.success(chapter);
    }
    
    /**
     * 删除章节
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除章节", description = "删除指定的章节")
    public Result<Void> deleteChapter(
            @Parameter(description = "章节ID") @PathVariable("id") Long chapterId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除章节, 章节ID: {}, 机构ID: {}", chapterId, institutionId);
        
        chapterService.deleteChapter(chapterId);
        
        return Result.success();
    }
    
    /**
     * 获取课程章节列表
     */
    @GetMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取课程章节列表", description = "获取指定课程的所有章节")
    public Result<List<ChapterVO>> getChaptersByCourse(
            @Parameter(description = "课程ID") @PathVariable("courseId") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取课程章节列表, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        List<ChapterVO> chapters = chapterService.getChaptersByCourse(courseId);
        
        return Result.success(chapters);
    }
    
    /**
     * 更新章节访问类型
     */
    @PutMapping("/{id}/access-type")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新章节访问类型", description = "更新指定章节的访问类型")
    public Result<ChapterVO> updateAccessType(
            @Parameter(description = "章节ID") @PathVariable("id") Long chapterId,
            @Parameter(description = "访问类型") @RequestParam Integer accessType) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新章节访问类型, 章节ID: {}, 机构ID: {}, 访问类型: {}", 
                chapterId, institutionId, accessType);
        
        ChapterVO chapter = chapterService.updateAccessType(chapterId, accessType);
        
        return Result.success(chapter);
    }
    
    /**
     * 调整章节顺序
     */
    @PutMapping("/course/{courseId}/reorder")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "调整章节顺序", description = "调整课程中章节的顺序")
    public Result<List<ChapterVO>> reorderChapters(
            @Parameter(description = "课程ID") @PathVariable("courseId") Long courseId,
            @Valid @RequestBody List<ChapterOrderDTO> chapterOrders) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("调整章节顺序, 课程ID: {}, 机构ID: {}, 章节数量: {}", 
                courseId, institutionId, chapterOrders.size());
        
        List<ChapterVO> chapters = chapterService.reorderChapters(courseId, chapterOrders);
        
        return Result.success(chapters);
    }
} 