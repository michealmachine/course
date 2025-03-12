package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.TagDTO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.TagVO;
import com.zhangziqi.online_course_mine.service.TagService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
@Tag(name = "标签管理", description = "课程标签的创建、查询、编辑、删除等功能")
public class TagController {

    private final TagService tagService;

    /**
     * 创建标签
     *
     * @param tagDTO 标签信息
     * @return 创建后的标签ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "创建标签", description = "创建新的课程标签")
    public Result<Map<String, Long>> createTag(@Valid @RequestBody TagDTO tagDTO) {
        log.info("创建标签: {}", tagDTO);
        Long id = tagService.createTag(tagDTO);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    /**
     * 更新标签
     *
     * @param id 标签ID
     * @param tagDTO 标签信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "更新标签", description = "更新标签信息")
    public Result<Void> updateTag(
            @Parameter(description = "标签ID") @PathVariable("id") Long id,
            @Valid @RequestBody TagDTO tagDTO) {
        log.info("更新标签: id={}, {}", id, tagDTO);
        tagService.updateTag(id, tagDTO);
        return Result.success();
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "删除标签", description = "删除指定的标签")
    public Result<Void> deleteTag(@Parameter(description = "标签ID") @PathVariable("id") Long id) {
        log.info("删除标签: id={}", id);
        tagService.deleteTag(id);
        return Result.success();
    }

    /**
     * 获取标签详情
     *
     * @param id 标签ID
     * @return 标签详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详情")
    public Result<TagVO> getTag(@Parameter(description = "标签ID") @PathVariable("id") Long id) {
        log.info("获取标签详情: id={}", id);
        TagVO tag = tagService.getTag(id);
        return Result.success(tag);
    }

    /**
     * 根据名称获取标签
     *
     * @param name 标签名称
     * @return 标签详情
     */
    @GetMapping("/name/{name}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "根据名称获取标签", description = "根据名称获取标签详情")
    public Result<TagVO> getTagByName(@Parameter(description = "标签名称") @PathVariable("name") String name) {
        log.info("根据名称获取标签: name={}", name);
        TagVO tag = tagService.getTagByName(name);
        return Result.success(tag);
    }

    /**
     * 分页查询标签
     *
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 标签列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "分页查询标签", description = "分页获取标签列表，支持关键词搜索")
    public Result<Page<TagVO>> listTags(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("分页查询标签: keyword={}", keyword);
        Page<TagVO> tags = tagService.listTags(keyword, pageable);
        return Result.success(tags);
    }

    /**
     * 获取热门标签
     *
     * @param limit 数量限制
     * @return 热门标签列表
     */
    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取热门标签", description = "获取使用最多的标签列表")
    public Result<List<TagVO>> getPopularTags(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        log.info("获取热门标签: limit={}", limit);
        List<TagVO> popularTags = tagService.getPopularTags(limit);
        return Result.success(popularTags);
    }

    /**
     * 检查标签名称是否可用
     *
     * @param name 标签名称
     * @param excludeId 排除的标签ID（更新时使用）
     * @return 是否可用
     */
    @GetMapping("/check-name")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "检查标签名称是否可用", description = "检查标签名称是否已被使用")
    public Result<Map<String, Boolean>> isNameAvailable(
            @Parameter(description = "标签名称") @RequestParam String name,
            @Parameter(description = "排除的标签ID") @RequestParam(required = false) Long excludeId) {
        log.info("检查标签名称是否可用: name={}, excludeId={}", name, excludeId);
        boolean available = tagService.isNameAvailable(name, excludeId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return Result.success(result);
    }

    /**
     * 批量获取或创建标签
     *
     * @param tagNames 标签名称列表
     * @return 标签ID列表
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER', 'INSTITUTION')")
    @Operation(summary = "批量获取或创建标签", description = "根据名称批量获取或创建标签")
    public Result<List<Long>> batchGetOrCreateTags(@RequestBody List<String> tagNames) {
        log.info("批量获取或创建标签: tagNames={}", tagNames);
        List<Long> tagIds = tagService.getOrCreateTags(tagNames).stream()
                .map(tag -> tag.getId())
                .toList();
        return Result.success(tagIds);
    }
} 