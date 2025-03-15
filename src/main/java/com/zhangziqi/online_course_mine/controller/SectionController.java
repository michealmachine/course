package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.section.SectionCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.section.SectionOrderDTO;
import com.zhangziqi.online_course_mine.model.dto.section.SectionQuestionGroupDTO;
import com.zhangziqi.online_course_mine.model.dto.section.SectionResourceDTO;
import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.SectionQuestionGroup;
import com.zhangziqi.online_course_mine.model.entity.SectionResource;
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
    public Result<Section> createSection(@Valid @RequestBody SectionCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("创建小节, 章节ID: {}, 机构ID: {}, 小节标题: {}", 
                dto.getChapterId(), institutionId, dto.getTitle());
        
        Section section = sectionService.createSection(dto);
        
        return Result.success(section);
    }
    
    /**
     * 获取小节详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取小节详情", description = "获取指定小节的详细信息")
    public Result<Section> getSectionById(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取小节详情, 小节ID: {}, 机构ID: {}", sectionId, institutionId);
        
        Section section = sectionService.getSectionById(sectionId);
        
        return Result.success(section);
    }
    
    /**
     * 更新小节
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新小节", description = "更新指定小节的信息")
    public Result<Section> updateSection(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId,
            @Valid @RequestBody SectionCreateDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新小节, 小节ID: {}, 机构ID: {}, 小节标题: {}", 
                sectionId, institutionId, dto.getTitle());
        
        Section section = sectionService.updateSection(sectionId, dto);
        
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
    public Result<List<Section>> getSectionsByChapter(
            @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取章节下的小节列表, 章节ID: {}, 机构ID: {}", chapterId, institutionId);
        
        List<Section> sections = sectionService.getSectionsByChapter(chapterId);
        
        return Result.success(sections);
    }
    
    /**
     * 获取课程下的所有小节
     */
    @GetMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取课程下的所有小节", description = "获取指定课程下的所有小节")
    public Result<List<Section>> getSectionsByCourse(
            @Parameter(description = "课程ID") @PathVariable("courseId") Long courseId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取课程下的所有小节, 课程ID: {}, 机构ID: {}", courseId, institutionId);
        
        List<Section> sections = sectionService.getSectionsByCourse(courseId);
        
        return Result.success(sections);
    }
    
    /**
     * 调整小节顺序
     */
    @PutMapping("/chapter/{chapterId}/reorder")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "调整小节顺序", description = "调整章节中小节的顺序")
    public Result<List<Section>> reorderSections(
            @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId,
            @Valid @RequestBody List<SectionOrderDTO> sectionOrders) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("调整小节顺序, 章节ID: {}, 机构ID: {}, 小节数量: {}", 
                chapterId, institutionId, sectionOrders.size());
        
        List<Section> sections = sectionService.reorderSections(chapterId, sectionOrders);
        
        return Result.success(sections);
    }
    
    /**
     * 添加小节资源
     */
    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "添加小节资源", description = "为小节添加媒体资源")
    public Result<SectionResource> addSectionResource(@Valid @RequestBody SectionResourceDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("添加小节资源, 小节ID: {}, 媒体ID: {}, 机构ID: {}", 
                dto.getSectionId(), dto.getMediaId(), institutionId);
        
        SectionResource resource = sectionService.addSectionResource(dto);
        
        return Result.success(resource);
    }
    
    /**
     * 获取小节资源列表
     */
    @GetMapping("/{id}/resources")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取小节资源列表", description = "获取指定小节的所有资源")
    public Result<List<SectionResource>> getSectionResources(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取小节资源列表, 小节ID: {}, 机构ID: {}", sectionId, institutionId);
        
        List<SectionResource> resources = sectionService.getSectionResources(sectionId);
        
        return Result.success(resources);
    }
    
    /**
     * 删除小节资源
     */
    @DeleteMapping("/resources/{resourceId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除小节资源", description = "删除指定的小节资源")
    public Result<Void> deleteSectionResource(
            @Parameter(description = "资源ID") @PathVariable("resourceId") Long resourceId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除小节资源, 资源ID: {}, 机构ID: {}", resourceId, institutionId);
        
        sectionService.deleteSectionResource(resourceId);
        
        return Result.success();
    }
    
    /**
     * 添加小节题目组
     */
    @PostMapping("/question-groups")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "添加小节题目组", description = "为小节添加题目组")
    public Result<SectionQuestionGroup> addSectionQuestionGroup(@Valid @RequestBody SectionQuestionGroupDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("添加小节题目组, 小节ID: {}, 题目组ID: {}, 机构ID: {}", 
                dto.getSectionId(), dto.getQuestionGroupId(), institutionId);
        
        SectionQuestionGroup questionGroup = sectionService.addSectionQuestionGroup(dto);
        
        return Result.success(questionGroup);
    }
    
    /**
     * 获取小节题目组列表
     */
    @GetMapping("/{id}/question-groups")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取小节题目组列表", description = "获取指定小节的所有题目组")
    public Result<List<SectionQuestionGroup>> getSectionQuestionGroups(
            @Parameter(description = "小节ID") @PathVariable("id") Long sectionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取小节题目组列表, 小节ID: {}, 机构ID: {}", sectionId, institutionId);
        
        List<SectionQuestionGroup> questionGroups = sectionService.getSectionQuestionGroups(sectionId);
        
        return Result.success(questionGroups);
    }
    
    /**
     * 更新小节题目组
     */
    @PutMapping("/{sectionId}/question-groups/{questionGroupId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新小节题目组", description = "更新小节的题目组设置")
    public Result<SectionQuestionGroup> updateSectionQuestionGroup(
            @Parameter(description = "小节ID") @PathVariable("sectionId") Long sectionId,
            @Parameter(description = "题目组ID") @PathVariable("questionGroupId") Long questionGroupId,
            @Valid @RequestBody SectionQuestionGroupDTO dto) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新小节题目组, 小节ID: {}, 题目组ID: {}, 机构ID: {}", 
                sectionId, questionGroupId, institutionId);
        
        SectionQuestionGroup questionGroup = sectionService.updateSectionQuestionGroup(sectionId, questionGroupId, dto);
        
        return Result.success(questionGroup);
    }
    
    /**
     * 删除小节题目组
     */
    @DeleteMapping("/{sectionId}/question-groups/{questionGroupId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除小节题目组", description = "删除小节的题目组关联")
    public Result<Void> deleteSectionQuestionGroup(
            @Parameter(description = "小节ID") @PathVariable("sectionId") Long sectionId,
            @Parameter(description = "题目组ID") @PathVariable("questionGroupId") Long questionGroupId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除小节题目组, 小节ID: {}, 题目组ID: {}, 机构ID: {}", 
                sectionId, questionGroupId, institutionId);
        
        sectionService.deleteSectionQuestionGroup(sectionId, questionGroupId);
        
        return Result.success();
    }
} 