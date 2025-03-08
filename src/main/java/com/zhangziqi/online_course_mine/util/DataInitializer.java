package com.zhangziqi.online_course_mine.util;

import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.repository.PermissionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * 数据初始化
 * 用于初始化角色和权限数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev", "test"}) // 仅在开发和测试环境下运行
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 初始化数据
     */
    @PostConstruct
    @Transactional
    public void init() {
        log.info("开始初始化角色和权限数据");
        
        // 初始化角色
        initRoles();
        
        // 初始化权限
        initPermissions();
        
        // 初始化角色权限关系
        initRolePermissions();
        
        // 初始化管理员
        initAdmin();
        
        log.info("角色和权限数据初始化完成");
    }

    /**
     * 初始化角色
     */
    private void initRoles() {
        // 检查角色是否已存在
        if (roleRepository.count() > 0) {
            log.info("角色数据已存在，跳过初始化");
            return;
        }

        // 创建角色
        List<Role> roles = (List<Role>) Arrays.stream(RoleEnum.values())
                .map(roleEnum -> Role.builder()
                        .name(roleEnum.getName())
                        .code(roleEnum.getCode())
                        .description(roleEnum.getName() + "角色")
                        .build())
                .toList();

        // 保存角色
        roleRepository.saveAll(roles);
        log.info("角色数据初始化完成，共{}条", roles.size());
    }

    /**
     * 初始化权限
     */
    private void initPermissions() {
        // 检查权限是否已存在
        if (permissionRepository.count() > 0) {
            log.info("权限数据已存在，跳过初始化");
            return;
        }

        // 创建权限
        List<Permission> permissions = Arrays.asList(
                Permission.builder().name("用户查询").code("user:read").url("/api/users/**").method("GET").build(),
                Permission.builder().name("用户创建").code("user:create").url("/api/users").method("POST").build(),
                Permission.builder().name("用户修改").code("user:update").url("/api/users/**").method("PUT").build(),
                Permission.builder().name("用户删除").code("user:delete").url("/api/users/**").method("DELETE").build(),
                Permission.builder().name("角色查询").code("role:read").url("/api/roles/**").method("GET").build(),
                Permission.builder().name("角色创建").code("role:create").url("/api/roles").method("POST").build(),
                Permission.builder().name("角色修改").code("role:update").url("/api/roles/**").method("PUT").build(),
                Permission.builder().name("角色删除").code("role:delete").url("/api/roles/**").method("DELETE").build(),
                Permission.builder().name("权限查询").code("permission:read").url("/api/permissions/**").method("GET").build(),
                Permission.builder().name("权限创建").code("permission:create").url("/api/permissions").method("POST").build(),
                Permission.builder().name("权限修改").code("permission:update").url("/api/permissions/**").method("PUT").build(),
                Permission.builder().name("权限删除").code("permission:delete").url("/api/permissions/**").method("DELETE").build(),
                Permission.builder().name("课程查询").code("course:read").url("/api/courses/**").method("GET").build(),
                Permission.builder().name("课程创建").code("course:create").url("/api/courses").method("POST").build(),
                Permission.builder().name("课程修改").code("course:update").url("/api/courses/**").method("PUT").build(),
                Permission.builder().name("课程删除").code("course:delete").url("/api/courses/**").method("DELETE").build(),
                Permission.builder().name("课程审核").code("course:review").url("/api/courses/*/review").method("POST").build()
        );

        // 保存权限
        permissionRepository.saveAll(permissions);
        log.info("权限数据初始化完成，共{}条", permissions.size());
    }

    /**
     * 初始化角色权限关系
     */
    private void initRolePermissions() {
        // 获取角色
        Role adminRole = roleRepository.findByCode(RoleEnum.ADMIN.getCode())
                .orElseThrow(() -> new RuntimeException("管理员角色不存在"));
        Role userRole = roleRepository.findByCode(RoleEnum.USER.getCode())
                .orElseThrow(() -> new RuntimeException("普通用户角色不存在"));
        Role reviewerRole = roleRepository.findByCode(RoleEnum.REVIEWER.getCode())
                .orElseThrow(() -> new RuntimeException("审核人员角色不存在"));
        Role institutionRole = roleRepository.findByCode(RoleEnum.INSTITUTION.getCode())
                .orElseThrow(() -> new RuntimeException("机构用户角色不存在"));

        // 获取所有权限
        List<Permission> allPermissions = permissionRepository.findAll();
        if (allPermissions.isEmpty()) {
            log.warn("权限数据为空，跳过初始化角色权限关系");
            return;
        }

        // 如果角色已经有权限，则跳过
        if (!adminRole.getPermissions().isEmpty()) {
            log.info("角色权限关系已存在，跳过初始化");
            return;
        }

        // 设置管理员角色的权限（所有权限）
        adminRole.setPermissions(new HashSet<>(allPermissions));

        // 设置普通用户角色的权限（查询权限）
        userRole.setPermissions(allPermissions.stream()
                .filter(permission -> permission.getCode().endsWith(":read"))
                .filter(permission -> !permission.getCode().startsWith("role:") && !permission.getCode().startsWith("permission:"))
                .collect(java.util.stream.Collectors.toSet()));

        // 设置审核人员角色的权限（查询权限 + 课程审核权限）
        reviewerRole.setPermissions(allPermissions.stream()
                .filter(permission -> permission.getCode().endsWith(":read") || permission.getCode().equals("course:review"))
                .collect(java.util.stream.Collectors.toSet()));

        // 设置机构用户角色的权限（查询权限 + 课程管理权限）
        institutionRole.setPermissions(allPermissions.stream()
                .filter(permission -> permission.getCode().endsWith(":read") || 
                                      permission.getCode().startsWith("course:"))
                .collect(java.util.stream.Collectors.toSet()));

        // 保存角色
        roleRepository.saveAll(Arrays.asList(adminRole, userRole, reviewerRole, institutionRole));
        log.info("角色权限关系初始化完成");
    }

    /**
     * 初始化管理员
     */
    private void initAdmin() {
        // 检查管理员是否已存在
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("管理员已存在，跳过初始化");
            return;
        }

        // 获取管理员角色
        Role adminRole = roleRepository.findByCode(RoleEnum.ADMIN.getCode())
                .orElseThrow(() -> new RuntimeException("管理员角色不存在"));

        // 创建管理员
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .status(1)
                .roles(Collections.singleton(adminRole))
                .build();

        // 保存管理员
        userRepository.save(admin);
        log.info("管理员初始化完成");
    }
} 