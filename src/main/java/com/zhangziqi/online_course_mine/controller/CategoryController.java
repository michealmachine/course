package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.CategoryDTO;
import com.zhangziqi.online_course_mine.model.vo.CategoryTreeVO;
import com.zhangziqi.online_course_mine.model.vo.CategoryVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.CategoryService;
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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 分类管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "分类管理", description = "课程分类的创建、查询、编辑、删除等功能")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建分类
     *
     * @param categoryDTO 分类信息
     * @return 创建后的分类ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "创建分类", description = "创建新的课程分类")
    public Result<Map<String, Long>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("创建分类: {}", categoryDTO);
        Long id = categoryService.createCategory(categoryDTO);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    /**
     * 更新分类
     *
     * @param id 分类ID
     * @param categoryDTO 分类信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "更新分类", description = "更新分类信息")
    public Result<Void> updateCategory(
            @Parameter(description = "分类ID") @PathVariable("id") Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("更新分类: id={}, {}", id, categoryDTO);
        categoryService.updateCategory(id, categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "删除分类", description = "删除指定的分类")
    public Result<Void> deleteCategory(@Parameter(description = "分类ID") @PathVariable("id") Long id) {
        log.info("删除分类: id={}", id);
        categoryService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER', 'INSTITUTION')")
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详情")
    public Result<CategoryVO> getCategory(@Parameter(description = "分类ID") @PathVariable("id") Long id) {
        log.info("获取分类详情: id={}", id);
        CategoryVO category = categoryService.getCategory(id);
        return Result.success(category);
    }

    /**
     * 根据编码获取分类
     *
     * @param code 分类编码
     * @return 分类详情
     */
    @GetMapping("/code/{code}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER', 'INSTITUTION')")
    @Operation(summary = "根据编码获取分类", description = "根据编码获取分类详情")
    public Result<CategoryVO> getCategoryByCode(@Parameter(description = "分类编码") @PathVariable("code") String code) {
        log.info("根据编码获取分类: code={}", code);
        CategoryVO category = categoryService.getCategoryByCode(code);
        return Result.success(category);
    }

    /**
     * 分页查询分类
     *
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 分类列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER', 'INSTITUTION')")
    @Operation(summary = "分页查询分类", description = "分页获取分类列表，支持关键词搜索")
    public Result<Page<CategoryVO>> listCategories(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("分页查询分类: keyword={}", keyword);
        Page<CategoryVO> categories = categoryService.listCategories(keyword, pageable);
        return Result.success(categories);
    }

    /**
     * 获取所有根分类
     *
     * @return 根分类列表
     */
    @GetMapping("/roots")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER', 'INSTITUTION')")
    @Operation(summary = "获取根分类", description = "获取所有顶级分类")
    public Result<List<CategoryVO>> listRootCategories() {
        log.info("获取所有根分类");
        List<CategoryVO> rootCategories = categoryService.listRootCategories();
        return Result.success(rootCategories);
    }

    /**
     * 获取子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/children/{parentId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER', 'INSTITUTION')")
    @Operation(summary = "获取子分类", description = "获取指定父分类下的所有子分类")
    public Result<List<CategoryVO>> listChildCategories(
            @Parameter(description = "父分类ID") @PathVariable("parentId") Long parentId) {
        log.info("获取子分类: parentId={}", parentId);
        List<CategoryVO> childCategories = categoryService.listChildCategories(parentId);
        return Result.success(childCategories);
    }

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    @GetMapping("/tree")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER', 'INSTITUTION')")
    @Operation(summary = "获取分类树", description = "获取完整的分类树结构")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        log.info("获取分类树");
        List<CategoryTreeVO> categoryTree = categoryService.getCategoryTree();
        return Result.success(categoryTree);
    }

    /**
     * 检查分类编码是否可用
     *
     * @param code 分类编码
     * @param excludeId 排除的分类ID（更新时使用）
     * @return 是否可用
     */
    @GetMapping("/check-code")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "检查分类编码是否可用", description = "检查分类编码是否已被使用")
    public Result<Map<String, Boolean>> isCodeAvailable(
            @Parameter(description = "分类编码") @RequestParam String code,
            @Parameter(description = "排除的分类ID") @RequestParam(required = false) Long excludeId) {
        log.info("检查分类编码是否可用: code={}, excludeId={}", code, excludeId);
        boolean available = categoryService.isCodeAvailable(code, excludeId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return Result.success(result);
    }

    /**
     * 更新分类状态
     *
     * @param id 分类ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "更新分类状态", description = "启用或禁用指定分类")
    public Result<Void> updateCategoryStatus(
            @Parameter(description = "分类ID") @PathVariable("id") Long id,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        log.info("更新分类状态: id={}, enabled={}", id, enabled);
        categoryService.updateCategoryStatus(id, enabled);
        return Result.success();
    }

    /**
     * 更新分类排序
     *
     * @param id 分类ID
     * @param orderIndex 排序索引
     * @return 操作结果
     */
    @PutMapping("/{id}/order")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @Operation(summary = "更新分类排序", description = "更新指定分类的排序索引")
    public Result<Void> updateCategoryOrder(
            @Parameter(description = "分类ID") @PathVariable("id") Long id,
            @Parameter(description = "排序索引") @RequestParam Integer orderIndex) {
        log.info("更新分类排序: id={}, orderIndex={}", id, orderIndex);
        categoryService.updateCategoryOrder(id, orderIndex);
        return Result.success();
    }
} 