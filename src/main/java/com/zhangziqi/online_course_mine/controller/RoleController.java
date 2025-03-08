package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.RoleDTO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.RoleVO;
import com.zhangziqi.online_course_mine.service.RoleService;
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
import java.util.Set;

/**
 * 角色管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@Tag(name = "角色管理", description = "角色查询、创建、编辑、删除等功能")
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取角色列表", description = "获取所有角色列表")
    public Result<List<RoleVO>> getRoleList() {
        log.info("获取角色列表");
        List<RoleVO> roleList = roleService.getRoleList();
        return Result.success(roleList);
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色详情")
    public Result<RoleVO> getRoleById(@Parameter(description = "角色ID") @PathVariable("id") Long id) {
        log.info("获取角色详情: {}", id);
        RoleVO roleVO = roleService.getRoleById(id);
        return Result.success(roleVO);
    }

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 创建后的角色信息
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建角色", description = "创建新角色")
    public Result<RoleVO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("创建角色: {}", roleDTO);
        RoleVO roleVO = roleService.createRole(roleDTO);
        return Result.success(roleVO);
    }

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param roleDTO 角色信息
     * @return 更新后的角色信息
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public Result<RoleVO> updateRole(
            @Parameter(description = "角色ID") @PathVariable("id") Long id,
            @Valid @RequestBody RoleDTO roleDTO) {
        log.info("更新角色: {}, {}", id, roleDTO);
        RoleVO roleVO = roleService.updateRole(id, roleDTO);
        return Result.success(roleVO);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 无
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除角色", description = "根据角色ID删除角色")
    public Result<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable("id") Long id) {
        log.info("删除角色: {}", id);
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 给角色分配权限
     *
     * @param id            角色ID
     * @param permissionIds 权限ID列表
     * @return 更新后的角色信息
     */
    @PutMapping("/{id}/permissions")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "给角色分配权限", description = "给角色分配权限")
    public Result<RoleVO> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable("id") Long id,
            @Parameter(description = "权限ID列表") @RequestBody Set<Long> permissionIds) {
        log.info("给角色分配权限: {}, {}", id, permissionIds);
        RoleVO roleVO = roleService.assignPermissions(id, permissionIds);
        return Result.success(roleVO);
    }

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     * @return 无
     */
    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除角色", description = "批量删除角色")
    public Result<Void> batchDeleteRoles(@Parameter(description = "角色ID列表") @RequestBody List<Long> ids) {
        log.info("批量删除角色: {}", ids);
        roleService.batchDeleteRoles(ids);
        return Result.success();
    }
} 