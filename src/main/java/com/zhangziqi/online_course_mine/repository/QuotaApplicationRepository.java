package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.QuotaApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 存储配额申请仓库
 */
@Repository
public interface QuotaApplicationRepository extends JpaRepository<QuotaApplication, Long> {
    
    /**
     * 根据申请ID和申请人ID查找申请
     */
    Optional<QuotaApplication> findByApplicationIdAndApplicantId(String applicationId, Long applicantId);
    
    /**
     * 根据机构ID分页查询申请
     */
    Page<QuotaApplication> findByInstitutionId(Long institutionId, Pageable pageable);
    
    /**
     * 根据机构ID和状态分页查询申请
     */
    Page<QuotaApplication> findByInstitutionIdAndStatus(Long institutionId, Integer status, Pageable pageable);
    
    /**
     * 根据申请人ID分页查询申请
     */
    Page<QuotaApplication> findByApplicantId(Long applicantId, Pageable pageable);
    
    /**
     * 根据申请人ID和状态分页查询申请
     */
    Page<QuotaApplication> findByApplicantIdAndStatus(Long applicantId, Integer status, Pageable pageable);
    
    /**
     * 根据状态分页查询申请
     */
    Page<QuotaApplication> findByStatus(Integer status, Pageable pageable);
} 