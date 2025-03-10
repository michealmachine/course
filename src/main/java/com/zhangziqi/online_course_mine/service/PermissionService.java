package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.PermissionDTO;
import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.vo.PermissionVO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    List<PermissionVO> getPermissionList();

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限详情
     */
    PermissionVO getPermissionById(Long id);

    /**
     * 获取权限（根据编码）
     *
     * @param code 权限编码
     * @return 权限
     */
    Permission getPermissionByCode(String code);

    /**
     * 创建权限
     *
     * @param permissionDTO 权限信息
     * @return 创建后的权限信息
     */
    PermissionVO createPermission(PermissionDTO permissionDTO);

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param permissionDTO 权限信息
     * @return 更新后的权限信息
     */
    PermissionVO updatePermission(Long id, PermissionDTO permissionDTO);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void deletePermission(Long id);
} 