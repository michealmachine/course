package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.PermissionDTO;
import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.vo.PermissionVO;
import com.zhangziqi.online_course_mine.repository.PermissionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "permissions") // 指定缓存名称
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all'") // 缓存所有权限列表
    public List<PermissionVO> getPermissionList() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(this::convertToPermissionVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限详情
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id") // 缓存单个权限
    public PermissionVO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        return convertToPermissionVO(permission);
    }

    /**
     * 获取权限（根据编码）
     *
     * @param code 权限编码
     * @return 权限
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'code:' + #code") // 缓存根据code查询的权限
    public Permission getPermissionByCode(String code) {
        return permissionRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("权限不存在"));
    }

    /**
     * 创建权限
     *
     * @param permissionDTO 权限信息
     * @return 创建后的权限信息
     */
    @Override
    @Transactional
    @CacheEvict(allEntries = true) // 清除所有权限缓存
    public PermissionVO createPermission(PermissionDTO permissionDTO) {
        // 检查权限编码是否存在
        if (permissionRepository.findByCode(permissionDTO.getCode()).isPresent()) {
            throw new BusinessException("权限编码已存在");
        }

        // 检查URL和方法是否已存在权限
        if (StringUtils.hasText(permissionDTO.getUrl()) && StringUtils.hasText(permissionDTO.getMethod())) {
            if (permissionRepository.findByUrlAndMethod(permissionDTO.getUrl(), permissionDTO.getMethod()).isPresent()) {
                throw new BusinessException("该URL和请求方法的权限已存在");
            }
        }

        // 创建权限
        Permission permission = Permission.builder()
                .name(permissionDTO.getName())
                .code(permissionDTO.getCode())
                .description(permissionDTO.getDescription())
                .url(permissionDTO.getUrl())
                .method(permissionDTO.getMethod())
                .build();

        // 保存权限
        Permission savedPermission = permissionRepository.save(permission);
        log.info("创建权限成功: {}", savedPermission.getName());
        return convertToPermissionVO(savedPermission);
    }

    /**
     * 更新权限
     *
     * @param id            权限ID
     * @param permissionDTO 权限信息
     * @return 更新后的权限信息
     */
    @Override
    @Transactional
    @CacheEvict(allEntries = true) // 清除所有权限缓存
    public PermissionVO updatePermission(Long id, PermissionDTO permissionDTO) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        // 检查权限编码是否存在
        if (StringUtils.hasText(permissionDTO.getCode()) &&
                !permission.getCode().equals(permissionDTO.getCode()) &&
                permissionRepository.findByCode(permissionDTO.getCode()).isPresent()) {
            throw new BusinessException("权限编码已存在");
        }

        // 检查URL和方法是否已存在权限
        if (StringUtils.hasText(permissionDTO.getUrl()) && StringUtils.hasText(permissionDTO.getMethod())) {
            if (!permission.getUrl().equals(permissionDTO.getUrl()) || 
                !permission.getMethod().equals(permissionDTO.getMethod())) {
                if (permissionRepository.findByUrlAndMethod(permissionDTO.getUrl(), permissionDTO.getMethod()).isPresent()) {
                    throw new BusinessException("该URL和请求方法的权限已存在");
                }
            }
        }

        // 更新基本信息
        if (StringUtils.hasText(permissionDTO.getName())) {
            permission.setName(permissionDTO.getName());
        }

        if (StringUtils.hasText(permissionDTO.getCode())) {
            permission.setCode(permissionDTO.getCode());
        }

        if (permissionDTO.getDescription() != null) {
            permission.setDescription(permissionDTO.getDescription());
        }

        if (StringUtils.hasText(permissionDTO.getUrl())) {
            permission.setUrl(permissionDTO.getUrl());
        }

        if (StringUtils.hasText(permissionDTO.getMethod())) {
            permission.setMethod(permissionDTO.getMethod());
        }

        // 保存权限
        Permission savedPermission = permissionRepository.save(permission);
        log.info("更新权限成功: {}", savedPermission.getName());
        return convertToPermissionVO(savedPermission);
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    @Override
    @Transactional
    @CacheEvict(allEntries = true) // 清除所有权限缓存
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        // 检查权限是否被角色引用
        List<Role> roles = roleRepository.findAll();
        for (Role role : roles) {
            if (role.getPermissions().contains(permission)) {
                throw new BusinessException("权限已被角色引用，无法删除");
            }
        }

        permissionRepository.delete(permission);
        log.info("删除权限成功: {}", permission.getName());
    }

    /**
     * 转换为权限VO
     *
     * @param permission 权限实体
     * @return 权限VO
     */
    private PermissionVO convertToPermissionVO(Permission permission) {
        return PermissionVO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .code(permission.getCode())
                .description(permission.getDescription())
                .url(permission.getUrl())
                .method(permission.getMethod())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
} 