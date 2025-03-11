package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.StorageQuota;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 存储配额Repository
 */
@Repository
public interface StorageQuotaRepository extends JpaRepository<StorageQuota, Long> {
    
    /**
     * 根据机构和配额类型查找配额
     *
     * @param institution 机构
     * @param type 配额类型
     * @return 存储配额
     */
    Optional<StorageQuota> findByInstitutionAndType(Institution institution, QuotaType type);
    
    /**
     * 根据机构查找所有配额
     *
     * @param institution 机构
     * @return 配额列表
     */
    List<StorageQuota> findByInstitution(Institution institution);
    
    /**
     * 查找机构的有效配额（未过期或无过期时间）
     *
     * @param institution 机构
     * @return 有效配额列表
     */
    @Query("SELECT sq FROM StorageQuota sq WHERE sq.institution = :institution AND (sq.expiresAt IS NULL OR sq.expiresAt > CURRENT_TIMESTAMP)")
    List<StorageQuota> findActiveQuotasByInstitution(@Param("institution") Institution institution);
} 