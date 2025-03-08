package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.RoleDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.vo.RoleVO;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> getRoleList();

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleVO getRoleById(Long id);

    /**
     * 获取角色（根据编码）
     *
     * @param code 角色编码
     * @return 角色
     */
    Role getRoleByCode(String code);

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 创建后的角色信息
     */
    RoleVO createRole(RoleDTO roleDTO);

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param roleDTO 角色信息
     * @return 更新后的角色信息
     */
    RoleVO updateRole(Long id, RoleDTO roleDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 给角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 更新后的角色信息
     */
    RoleVO assignPermissions(Long roleId, Set<Long> permissionIds);

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     */
    void batchDeleteRoles(List<Long> ids);
} 