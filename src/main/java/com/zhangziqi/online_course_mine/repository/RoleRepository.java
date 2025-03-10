package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Permission;
import com.zhangziqi.online_course_mine.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色Repository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色编码查找角色
     *
     * @param code 角色编码
     * @return 角色
     */
    Optional<Role> findByCode(String code);

    /**
     * 根据角色名称查找角色
     *
     * @param name 角色名称
     * @return 角色
     */
    Optional<Role> findByName(String name);
    
    /**
     * 查找包含指定权限的所有角色
     * 
     * @param permission 权限
     * @return 角色列表
     */
    List<Role> findByPermissionsContaining(Permission permission);
} 