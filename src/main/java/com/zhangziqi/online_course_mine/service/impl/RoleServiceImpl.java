package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.RoleDTO;
import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.vo.RoleVO;
import com.zhangziqi.online_course_mine.repository.PermissionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoleVO> getRoleList() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToRoleVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @Override
    @Transactional(readOnly = true)
    public RoleVO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        return convertToRoleVO(role);
    }

    /**
     * 获取角色（根据编码）
     *
     * @param code 角色编码
     * @return 角色
     */
    @Override
    @Transactional(readOnly = true)
    public Role getRoleByCode(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("角色不存在"));
    }

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 创建后的角色信息
     */
    @Override
    @Transactional
    public RoleVO createRole(RoleDTO roleDTO) {
        // 检查角色编码是否存在
        if (roleRepository.findByCode(roleDTO.getCode()).isPresent()) {
            throw new BusinessException("角色编码已存在");
        }

        // 检查角色名称是否存在
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new BusinessException("角色名称已存在");
        }

        // 获取权限
        Set<Permission> permissions = new HashSet<>();
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            permissions = roleDTO.getPermissionIds().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new BusinessException("权限不存在: " + permissionId)))
                    .collect(Collectors.toSet());
        }

        // 创建角色
        Role role = Role.builder()
                .name(roleDTO.getName())
                .code(roleDTO.getCode())
                .description(roleDTO.getDescription())
                .permissions(permissions)
                .build();

        // 保存角色
        Role savedRole = roleRepository.save(role);
        log.info("创建角色成功: {}", savedRole.getName());
        return convertToRoleVO(savedRole);
    }

    /**
     * 更新角色
     *
     * @param id      角色ID
     * @param roleDTO 角色信息
     * @return 更新后的角色信息
     */
    @Override
    @Transactional
    public RoleVO updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        // 检查角色编码是否存在
        if (StringUtils.hasText(roleDTO.getCode()) &&
                !role.getCode().equals(roleDTO.getCode()) &&
                roleRepository.findByCode(roleDTO.getCode()).isPresent()) {
            throw new BusinessException("角色编码已存在");
        }

        // 检查角色名称是否存在
        if (StringUtils.hasText(roleDTO.getName()) &&
                !role.getName().equals(roleDTO.getName()) &&
                roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new BusinessException("角色名称已存在");
        }

        // 更新权限
        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = roleDTO.getPermissionIds().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new BusinessException("权限不存在: " + permissionId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        // 更新基本信息
        if (StringUtils.hasText(roleDTO.getName())) {
            role.setName(roleDTO.getName());
        }

        if (StringUtils.hasText(roleDTO.getCode())) {
            role.setCode(roleDTO.getCode());
        }

        if (StringUtils.hasText(roleDTO.getDescription())) {
            role.setDescription(roleDTO.getDescription());
        }

        // 保存角色
        Role savedRole = roleRepository.save(role);
        log.info("更新角色成功: {}", savedRole.getName());
        return convertToRoleVO(savedRole);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        // 检查角色是否被用户引用
        if (userRepository.findAll().stream().anyMatch(user -> user.getRoles().contains(role))) {
            throw new BusinessException("角色已被用户引用，无法删除");
        }

        roleRepository.delete(role);
        log.info("删除角色成功: {}", role.getName());
    }

    /**
     * 给角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 更新后的角色信息
     */
    @Override
    @Transactional
    public RoleVO assignPermissions(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));

        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException("权限ID列表不能为空");
        }

        Set<Permission> permissions = permissionIds.stream()
                .map(permissionId -> permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new BusinessException("权限不存在: " + permissionId)))
                .collect(Collectors.toSet());

        role.setPermissions(permissions);
        Role savedRole = roleRepository.save(role);
        log.info("给角色分配权限成功: {}, 权限IDs: {}", savedRole.getName(), permissionIds);
        return convertToRoleVO(savedRole);
    }

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     */
    @Override
    @Transactional
    public void batchDeleteRoles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("角色ID列表不能为空");
        }

        for (Long id : ids) {
            deleteRole(id);
        }
    }

    /**
     * 转换为角色VO
     *
     * @param role 角色实体
     * @return 角色VO
     */
    private RoleVO convertToRoleVO(Role role) {
        return RoleVO.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .permissions(role.getPermissions())
                .build();
    }
} 