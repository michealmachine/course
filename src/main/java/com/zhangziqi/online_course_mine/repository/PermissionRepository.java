package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 权限Repository
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限编码查找权限
     *
     * @param code 权限编码
     * @return 权限
     */
    Optional<Permission> findByCode(String code);

    /**
     * 根据URL和请求方法查找权限
     *
     * @param url URL
     * @param method 请求方法
     * @return 权限
     */
    Optional<Permission> findByUrlAndMethod(String url, String method);
    
    /**
     * 根据权限编码查询，排除指定ID的权限
     * 
     * @param code 权限编码
     * @param id 要排除的权限ID
     * @return 权限
     */
    Optional<Permission> findByCodeAndIdNot(String code, Long id);
} 