package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQueryDTO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户查询、创建、编辑、删除等功能")
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户列表（分页）
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户列表")
    public Result<Page<UserVO>> getUserList(@Valid UserQueryDTO queryDTO) {
        log.info("分页查询用户列表: {}", queryDTO);
        Page<UserVO> page = userService.getUserList(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详情")
    public Result<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable("id") Long id) {
        log.info("获取用户详情: {}", id);
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 创建用户
     *
     * @param userDTO 用户信息
     * @return 创建后的用户信息
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建用户", description = "创建新用户")
    public Result<UserVO> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("创建用户: {}", userDTO);
        UserVO userVO = userService.createUser(userDTO);
        return Result.success(userVO);
    }

    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param userDTO 用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable("id") Long id,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("更新用户: {}, {}", id, userDTO);
        UserVO userVO = userService.updateUser(id, userDTO);
        return Result.success(userVO);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 无
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable("id") Long id) {
        log.info("删除用户: {}", id);
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 修改用户状态
     *
     * @param id 用户ID
     * @param status 状态（0-禁用，1-正常）
     * @return 更新后的用户信息
     */
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改用户状态", description = "修改用户状态（0-禁用，1-正常）")
    public Result<UserVO> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable("id") Long id,
            @Parameter(description = "状态（0-禁用，1-正常）") @RequestParam("status") Integer status) {
        log.info("修改用户状态: {}, {}", id, status);
        UserVO userVO = userService.updateUserStatus(id, status);
        return Result.success(userVO);
    }

    /**
     * 给用户分配角色
     *
     * @param id 用户ID
     * @param roleIds 角色ID列表
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}/roles")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "给用户分配角色", description = "给用户分配角色")
    public Result<UserVO> assignRoles(
            @Parameter(description = "用户ID") @PathVariable("id") Long id,
            @Parameter(description = "角色ID列表") @RequestBody Set<Long> roleIds) {
        log.info("给用户分配角色: {}, {}", id, roleIds);
        UserVO userVO = userService.assignRoles(id, roleIds);
        return Result.success(userVO);
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 无
     */
    @DeleteMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除用户", description = "批量删除用户")
    public Result<Void> batchDeleteUsers(@Parameter(description = "用户ID列表") @RequestBody List<Long> ids) {
        log.info("批量删除用户: {}", ids);
        userService.batchDeleteUsers(ids);
        return Result.success();
    }
} 