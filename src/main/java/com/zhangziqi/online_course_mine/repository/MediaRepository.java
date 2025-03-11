package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 媒体资源Repository
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    
    /**
     * 根据机构查找媒体资源
     *
     * @param institution 机构实体
     * @param pageable 分页参数
     * @return 媒体资源分页
     */
    Page<Media> findByInstitution(Institution institution, Pageable pageable);
    
    /**
     * 根据ID和机构查找媒体资源
     *
     * @param id 媒体资源ID
     * @param institution 机构实体
     * @return 媒体资源
     */
    Optional<Media> findByIdAndInstitution(Long id, Institution institution);
    
    /**
     * 计算机构的媒体资源总大小
     *
     * @param institution 机构实体
     * @return 总大小(字节)
     */
    @Query("SELECT SUM(m.size) FROM Media m WHERE m.institution = :institution")
    Long sumSizeByInstitution(@Param("institution") Institution institution);
    
    /**
     * 根据机构ID计算媒体资源总大小
     *
     * @param institutionId 机构ID
     * @return 总大小(字节)
     */
    @Query("SELECT SUM(m.size) FROM Media m WHERE m.institution.id = :institutionId")
    Long sumSizeByInstitutionId(@Param("institutionId") Long institutionId);
    
    /**
     * 根据上传者ID查找媒体资源
     *
     * @param uploaderId 上传者ID
     * @param pageable 分页参数
     * @return 媒体资源分页
     */
    Page<Media> findByUploaderId(Long uploaderId, Pageable pageable);
} 