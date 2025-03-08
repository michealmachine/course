package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.PermissionDTO;
import com.zhangziqi.online_course_mine.model.vo.PermissionVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.PermissionService;
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
 * 权限管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
@Tag(name = "权限管理", description = "权限查询、创建、编辑、删除等功能")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取权限列表", description = "获取所有权限列表")
    public Result<List<PermissionVO>> getPermissionList() {
        log.info("获取权限列表");
        List<PermissionVO> permissionList = permissionService.getPermissionList();
        return Result.success(permissionList);
    }

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取权限详情", description = "根据权限ID获取权限详情")
    public Result<PermissionVO> getPermissionById(@Parameter(description = "权限ID") @PathVariable("id") Long id) {
        log.info("获取权限详情: {}", id);
        PermissionVO permissionVO = permissionService.getPermissionById(id);
        return Result.success(permissionVO);
    }

    /**
     * 创建权限
     *
     * @param permissionDTO 权限信息
     * @return 创建后的权限信息
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建权限", description = "创建新权限")
    public Result<PermissionVO> createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("创建权限: {}", permissionDTO);
        PermissionVO permissionVO = permissionService.createPermission(permissionDTO);
        return Result.success(permissionVO);
    }

    /**
     * 更新权限
     *
     * @param id            权限ID
     * @param permissionDTO 权限信息
     * @return 更新后的权限信息
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新权限", description = "更新权限信息")
    public Result<PermissionVO> updatePermission(
            @Parameter(description = "权限ID") @PathVariable("id") Long id,
            @Valid @RequestBody PermissionDTO permissionDTO) {
        log.info("更新权限: {}, {}", id, permissionDTO);
        PermissionVO permissionVO = permissionService.updatePermission(id, permissionDTO);
        return Result.success(permissionVO);
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 无
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除权限", description = "根据权限ID删除权限")
    public Result<Void> deletePermission(@Parameter(description = "权限ID") @PathVariable("id") Long id) {
        log.info("删除权限: {}", id);
        permissionService.deletePermission(id);
        return Result.success();
    }

    /**
     * 批量删除权限
     *
     * @param ids 权限ID列表
     * @return 无
     */
    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除权限", description = "批量删除权限")
    public Result<Void> batchDeletePermissions(@Parameter(description = "权限ID列表") @RequestBody List<Long> ids) {
        log.info("批量删除权限: {}", ids);
        permissionService.batchDeletePermissions(ids);
        return Result.success();
    }
} 