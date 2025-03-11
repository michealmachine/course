package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 机构Repository
 */
@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    /**
     * 根据机构名称查找机构
     *
     * @param name 机构名称
     * @return 机构
     */
    Optional<Institution> findByName(String name);

    /**
     * 根据状态查找机构列表
     *
     * @param status 状态
     * @return 机构列表
     */
    List<Institution> findByStatus(Integer status);
    
    /**
     * 根据注册码查找机构
     *
     * @param registerCode 注册码
     * @return 机构
     */
    Optional<Institution> findByRegisterCode(String registerCode);
    
    /**
     * 检查注册码是否存在
     *
     * @param registerCode 注册码
     * @return 是否存在
     */
    boolean existsByRegisterCode(String registerCode);
} 