package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO;
import com.zhangziqi.online_course_mine.model.vo.QuestionTagVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.QuestionTagService;
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
 * 题目标签控制器
 * 处理题目标签的创建、更新、查询和管理请求
 */
@Slf4j
@RestController
@RequestMapping("/api/questions/tags")
@RequiredArgsConstructor
@Tag(name = "题目标签管理", description = "题目标签的创建、更新、查询和管理相关操作")
public class QuestionTagController {

    private final QuestionTagService tagService;

    /**
     * 创建标签
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "创建标签", description = "创建一个新的题目标签")
    public Result<QuestionTagVO> createTag(@Valid @RequestBody QuestionTagDTO tagDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("创建标签, 用户ID: {}, 机构ID: {}, 标签名称: {}", 
                userId, institutionId, tagDTO.getName());
        
        // 设置机构ID
        tagDTO.setInstitutionId(institutionId);
        
        QuestionTagVO tagVO = tagService.createTag(tagDTO, userId);
        return Result.success(tagVO);
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "更新标签", description = "更新指定ID的标签信息")
    public Result<QuestionTagVO> updateTag(
            @Parameter(description = "标签ID") @PathVariable("id") Long id,
            @Valid @RequestBody QuestionTagDTO tagDTO) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("更新标签, 标签ID: {}, 机构ID: {}, 标签名称: {}", 
                id, institutionId, tagDTO.getName());
        
        // 设置标签ID和机构ID
        tagDTO.setId(id);
        tagDTO.setInstitutionId(institutionId);
        
        QuestionTagVO tagVO = tagService.updateTag(tagDTO);
        return Result.success(tagVO);
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取标签详情", description = "获取指定ID的标签详细信息")
    public Result<QuestionTagVO> getTag(
            @Parameter(description = "标签ID") @PathVariable("id") Long id) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取标签详情, 标签ID: {}, 机构ID: {}", id, institutionId);
        
        QuestionTagVO tagVO = tagService.getTagById(id, institutionId);
        return Result.success(tagVO);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "删除标签", description = "删除指定ID的标签")
    public Result<Void> deleteTag(
            @Parameter(description = "标签ID") @PathVariable("id") Long id) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("删除标签, 标签ID: {}, 机构ID: {}", id, institutionId);
        
        tagService.deleteTag(id, institutionId);
        return Result.success();
    }

    /**
     * 分页查询标签列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取标签列表", description = "分页获取机构的标签列表")
    public Result<Page<QuestionTagVO>> getTags(
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取标签列表, 机构ID: {}, 关键词: {}, 页码: {}, 每页条数: {}", 
                institutionId, keyword, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionTagVO> tagPage = tagService.getTags(institutionId, keyword, pageable);
        return Result.success(tagPage);
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取所有标签", description = "获取机构的所有标签")
    public Result<List<QuestionTagVO>> getAllTags() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取所有标签, 机构ID: {}", institutionId);
        
        List<QuestionTagVO> tags = tagService.getAllTags(institutionId);
        return Result.success(tags);
    }

    /**
     * 获取题目的标签
     */
    @GetMapping("/question/{questionId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "获取题目标签", description = "获取指定题目关联的所有标签")
    public Result<List<QuestionTagVO>> getTagsByQuestionId(
            @Parameter(description = "题目ID") @PathVariable("questionId") Long questionId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("获取题目标签, 题目ID: {}, 机构ID: {}", questionId, institutionId);
        
        List<QuestionTagVO> tags = tagService.getTagsByQuestionId(questionId);
        return Result.success(tags);
    }

    /**
     * 为题目添加标签
     */
    @PostMapping("/question/{questionId}/tag/{tagId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "添加标签到题目", description = "为题目添加指定标签")
    public Result<Boolean> addTagToQuestion(
            @Parameter(description = "题目ID") @PathVariable("questionId") Long questionId,
            @Parameter(description = "标签ID") @PathVariable("tagId") Long tagId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("添加标签到题目, 题目ID: {}, 标签ID: {}, 机构ID: {}", 
                questionId, tagId, institutionId);
        
        boolean result = tagService.addTagToQuestion(questionId, tagId, institutionId);
        return Result.success(result);
    }

    /**
     * 移除题目标签
     */
    @DeleteMapping("/question/{questionId}/tag/{tagId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @Operation(summary = "移除题目标签", description = "从题目中移除指定标签")
    public Result<Boolean> removeTagFromQuestion(
            @Parameter(description = "题目ID") @PathVariable("questionId") Long questionId,
            @Parameter(description = "标签ID") @PathVariable("tagId") Long tagId) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        
        log.info("移除题目标签, 题目ID: {}, 标签ID: {}, 机构ID: {}", 
                questionId, tagId, institutionId);
        
        boolean result = tagService.removeTagFromQuestion(questionId, tagId, institutionId);
        return Result.success(result);
    }
} 