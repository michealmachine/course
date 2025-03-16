package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.section.*;
import com.zhangziqi.online_course_mine.model.vo.SectionQuestionGroupVO;
import com.zhangziqi.online_course_mine.model.vo.SectionResourceVO;
import com.zhangziqi.online_course_mine.model.vo.SectionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.SectionService;
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
 * 小节控制器
 * 处理课程小节及其资源的创建、更新、查询和管理
 */
@Slf4j
@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "小节管理", description = "课程小节及其资源的创建、更新、查询和管理相关操作")
public class SectionController {
    
    private final SectionService sectionService;
    
    /**
     * 创建小节
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "创建小节", description = "创建一个新的小节")
    public Result<SectionVO> createSection(@Valid @RequestBody SectionCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("创建小节, 章节ID: {}, 机构ID: {}, 小节标题: {}", 
                dto.getChapterId(), institutionId, dto.getTitle());
        
        SectionVO section = sectionService.createSection(dto);
        
        return Result.success(section);
    }
    
    /**
     * 获取小节详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取小节详情", description = "获取指定小节的详细信息")
    public Result<SectionVO> getSectionById(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取小节详情, 小节ID: {}, 机构ID: {}", sectionId, institutionId);
        
        SectionVO section = sectionService.getSectionById(sectionId);
        
        return Result.success(section);
    }
    
    /**
     * 更新小节
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新小节", description = "更新指定小节的信息")
    public Result<SectionVO> updateSection(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId,
            @Valid @RequestBody SectionCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新小节, 小节ID: {}, 机构ID: {}, 小节标题: {}", 
                sectionId, institutionId, dto.getTitle());
        
        SectionVO section = sectionService.updateSection(sectionId, dto);
        
        return Result.success(section);
    }
    
    /**
     * 删除小节
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除小节", description = "删除指定的小节")
    public Result<Void> deleteSection(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除小节, 小节ID: {}, 机构ID: {}", sectionId, institutionId);
        
        sectionService.deleteSection(sectionId);
        
        return Result.success();
    }
    
    /**
     * 获取章节下的小节列表
     */
    @GetMapping("/chapter/{chapterId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取章节下的小节列表", description = "获取指定章节下的所有小节")
    public Result<List<SectionVO>> getSectionsByChapter(
            @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取章节下的小节列表, 章节ID: {}, 机构ID: {}", chapterId, institutionId);
        
        List<SectionVO> sections = sectionService.getSectionsByChapter(chapterId);
        
        return Result.success(sections);
    }
    
    /**
     * 获取课程下的所有小节
     */
    @GetMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取课程下的所有小节", description = "获取指定课程下的所有小节")
    public Result<List<SectionVO>> getSectionsByCourse(
            @Parameter(description = "课程ID") @PathVariable("courseId") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取课程下的所有小节, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        List<SectionVO> sections = sectionService.getSectionsByCourse(courseId);
        
        return Result.success(sections);
    }
    
    /**
     * 调整小节顺序
     */
    @PutMapping("/chapter/{chapterId}/reorder")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "调整小节顺序", description = "调整章节中小节的顺序")
    public Result<List<SectionVO>> reorderSections(
            @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId,
            @Valid @RequestBody List<SectionOrderDTO> sectionOrders) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("调整小节顺序, 章节ID: {}, 机构ID: {}, 小节数量: {}", 
                chapterId, institutionId, sectionOrders.size());
        
        List<SectionVO> sections = sectionService.reorderSections(chapterId, sectionOrders);
        
        return Result.success(sections);
    }
    
    /**
     * 设置小节媒体资源（直接关联）
     */
    @PutMapping("/{id}/media/{mediaId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "设置小节媒体资源", description = "为小节设置直接关联的媒体资源")
    public Result<SectionVO> setMediaResource(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId,
            @Parameter(description = "媒体资源ID") @PathVariable("mediaId") Long mediaId,
            @Parameter(description = "资源类型") @RequestParam(required = true) String resourceType) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("设置小节媒体资源, 小节ID: {}, 媒体ID: {}, 资源类型: {}, 机构ID: {}", 
                sectionId, mediaId, resourceType, institutionId);
        
        SectionVO section = sectionService.setMediaResource(sectionId, mediaId, resourceType);
        
        return Result.success(section);
    }
    
    /**
     * 移除小节媒体资源（直接关联）
     */
    @DeleteMapping("/{id}/media")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "移除小节媒体资源", description = "移除小节直接关联的媒体资源")
    public Result<SectionVO> removeMediaResource(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("移除小节媒体资源, 小节ID: {}, 机构ID: {}", sectionId, institutionId);
        
        SectionVO section = sectionService.removeMediaResource(sectionId);
        
        return Result.success(section);
    }
    
    /**
     * 设置小节题目组（直接关联）
     */
    @PutMapping("/{id}/question-group/{questionGroupId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "设置小节题目组", description = "为小节设置直接关联的题目组")
    public Result<SectionVO> setQuestionGroup(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId,
            @Parameter(description = "题目组ID") @PathVariable("questionGroupId") Long questionGroupId,
            @Valid @RequestBody(required = false) SectionQuestionGroupConfigDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("设置小节题目组, 小节ID: {}, 题目组ID: {}, 机构ID: {}", 
                sectionId, questionGroupId, institutionId);
        
        SectionVO section = sectionService.setQuestionGroup(sectionId, questionGroupId, dto);
        
        return Result.success(section);
    }
    
    /**
     * 移除小节题目组（直接关联）
     */
    @DeleteMapping("/{id}/question-group")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "移除小节题目组", description = "移除小节直接关联的题目组")
    public Result<SectionVO> removeQuestionGroup(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("移除小节题目组, 小节ID: {}, 机构ID: {}", sectionId, institutionId);
        
        SectionVO section = sectionService.removeQuestionGroup(sectionId);
        
        return Result.success(section);
    }
} 