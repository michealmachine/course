package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.ChangePasswordDTO;
import com.zhangziqi.online_course_mine.model.dto.EmailUpdateDTO;
import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserProfileDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQueryDTO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.service.MinioService;
import com.zhangziqi.online_course_mine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private final MinioService minioService;

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

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserVO> getCurrentUser() {
        String username = getCurrentUsername();
        log.info("获取当前用户信息: {}", username);
        UserVO userVO = userService.getCurrentUser(username);
        return Result.success(userVO);
    }

    /**
     * 更新当前用户信息
     *
     * @param profileDTO 用户个人信息
     * @return 更新后的用户信息
     */
    @PutMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的个人信息")
    public Result<UserVO> updateCurrentUser(@Valid @RequestBody UserProfileDTO profileDTO) {
        String username = getCurrentUsername();
        log.info("更新当前用户信息: {}, {}", username, profileDTO);
        UserVO userVO = userService.updateCurrentUserProfile(username, profileDTO.getNickname(), profileDTO.getPhone());
        return Result.success(userVO);
    }

    /**
     * 修改当前用户密码
     *
     * @param changePasswordDTO 密码修改请求
     * @return 操作结果
     */
    @PutMapping("/current/password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        String username = getCurrentUsername();
        
        // 校验新密码与确认密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            return Result.fail(400, "新密码与确认密码不一致");
        }
        
        log.info("修改当前用户密码: {}", username);
        boolean result = userService.changePassword(username, 
                changePasswordDTO.getOldPassword(), 
                changePasswordDTO.getNewPassword());
        
        return result ? Result.success() : Result.fail(400, "密码修改失败");
    }

    /**
     * 更新当前用户邮箱
     *
     * @param emailUpdateDTO 邮箱更新请求
     * @return 操作结果
     */
    @PutMapping("/current/email")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "更新邮箱", description = "更新当前用户邮箱（需验证码）")
    public Result<UserVO> updateEmail(@Valid @RequestBody EmailUpdateDTO emailUpdateDTO) {
        String username = getCurrentUsername();
        log.info("更新当前用户邮箱: {}, 新邮箱: {}", username, emailUpdateDTO.getNewEmail());
        
        UserVO userVO = userService.updateEmail(username, 
                emailUpdateDTO.getNewEmail(), 
                emailUpdateDTO.getEmailCode(), 
                emailUpdateDTO.getPassword());
        
        return Result.success(userVO);
    }

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 头像URL
     */
    @PostMapping(value = "/current/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "上传头像", description = "上传当前用户头像")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String username = getCurrentUsername();
        log.info("上传头像: {}, 文件大小: {}", username, file.getSize());
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "只支持上传图片文件");
        }
        
        // 检查文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.fail(400, "文件大小不能超过2MB");
        }
        
        try {
            // 生成唯一的对象名
            String objectName = "avatars/" + username + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            
            // 上传到MinIO
            String avatarUrl = minioService.uploadFile(objectName, file.getInputStream(), file.getContentType());
            
            // 更新用户头像
            userService.updateAvatar(username, avatarUrl);
            
            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            return Result.success(result);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.fail(500, "头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户基本信息（用于前端展示）
     *
     * @param userId 用户ID
     * @return 用户基本信息
     */
    @GetMapping("/basic/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取用户基本信息", description = "获取用户基本信息（用于前端展示）")
    public Result<UserVO> getBasicUserInfo(@PathVariable("userId") Long userId) {
        log.info("获取用户基本信息: {}", userId);
        UserVO userVO = userService.getBasicUserInfo(userId);
        return Result.success(userVO);
    }
    
    /**
     * 获取当前登录用户名
     *
     * @return 当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
} 