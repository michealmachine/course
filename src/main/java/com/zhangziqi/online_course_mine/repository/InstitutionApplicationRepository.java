package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.InstitutionApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 机构申请Repository
 */
@Repository
public interface InstitutionApplicationRepository extends JpaRepository<InstitutionApplication, Long> {

    /**
     * 根据申请ID查找申请
     *
     * @param applicationId 申请ID
     * @return 申请
     */
    Optional<InstitutionApplication> findByApplicationId(String applicationId);
    
    /**
     * 根据申请ID和联系邮箱查找申请
     *
     * @param applicationId 申请ID
     * @param contactEmail 联系邮箱
     * @return 申请
     */
    Optional<InstitutionApplication> findByApplicationIdAndContactEmail(String applicationId, String contactEmail);
    
    /**
     * 根据状态查找申请列表
     *
     * @param status 状态
     * @return 申请列表
     */
    List<InstitutionApplication> findByStatus(Integer status);
    
    /**
     * 根据状态分页查询申请
     *
     * @param status 状态
     * @param pageable 分页
     * @return 申请分页
     */
    Page<InstitutionApplication> findByStatus(Integer status, Pageable pageable);
} 