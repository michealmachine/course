package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Media;
import com.zhangziqi.online_course_mine.model.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 媒体资源Repository
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long>, JpaSpecificationExecutor<Media> {
    
    /**
     * 根据机构查找媒体资源
     *
     * @param institution 机构实体
     * @param pageable 分页参数
     * @return 媒体资源分页
     */
    Page<Media> findByInstitution(Institution institution, Pageable pageable);
    
    /**
     * 根据机构和类型查找媒体资源
     *
     * @param institution 机构实体
     * @param type 媒体类型
     * @param pageable 分页参数
     * @return 媒体资源分页
     */
    Page<Media> findByInstitutionAndType(Institution institution, MediaType type, Pageable pageable);
    
    /**
     * 根据机构和文件名（模糊查询）查找媒体资源
     *
     * @param institution 机构实体
     * @param filename 文件名（部分匹配）
     * @param pageable 分页参数
     * @return 媒体资源分页
     */
    Page<Media> findByInstitutionAndOriginalFilenameContaining(Institution institution, String filename, Pageable pageable);
    
    /**
     * 根据机构、类型和文件名（模糊查询）查找媒体资源
     *
     * @param institution 机构实体
     * @param type 媒体类型
     * @param filename 文件名（部分匹配）
     * @param pageable 分页参数
     * @return 媒体资源分页
     */
    Page<Media> findByInstitutionAndTypeAndOriginalFilenameContaining(Institution institution, MediaType type, String filename, Pageable pageable);
    
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
    
    /**
     * 查询指定日期范围内机构的媒体上传活动
     * 
     * @param institutionId 机构ID
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 每日活动数据列表
     */
    @Query("SELECT new com.zhangziqi.online_course_mine.model.dto.media.MediaActivityDTO(" +
           "CAST(m.uploadTime AS LocalDate), " +
           "COUNT(m), " +
           "SUM(m.size)) " +
           "FROM Media m " +
           "WHERE m.institution.id = :institutionId " +
           "AND m.uploadTime BETWEEN :startDateTime AND :endDateTime " +
           "GROUP BY CAST(m.uploadTime AS LocalDate) " +
           "ORDER BY CAST(m.uploadTime AS LocalDate)")
    List<com.zhangziqi.online_course_mine.model.dto.media.MediaActivityDTO> findMediaUploadActivitiesByInstitution(
            @Param("institutionId") Long institutionId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * 查询指定日期范围内所有机构的媒体上传活动
     * 
     * @param startDateTime 开始时间
     * @param endDateTime 结束时间
     * @return 每日活动数据列表
     */
    @Query("SELECT new com.zhangziqi.online_course_mine.model.dto.media.MediaActivityDTO(" +
           "CAST(m.uploadTime AS LocalDate), " +
           "COUNT(m), " +
           "SUM(m.size)) " +
           "FROM Media m " +
           "WHERE m.uploadTime BETWEEN :startDateTime AND :endDateTime " +
           "GROUP BY CAST(m.uploadTime AS LocalDate) " +
           "ORDER BY CAST(m.uploadTime AS LocalDate)")
    List<com.zhangziqi.online_course_mine.model.dto.media.MediaActivityDTO> findAllMediaUploadActivities(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * 根据日期查询机构的媒体列表
     * 
     * @param institutionId 机构ID
     * @param date 日期
     * @param pageable 分页参数
     * @return 媒体分页
     */
    @Query("SELECT m FROM Media m " +
           "WHERE m.institution.id = :institutionId " +
           "AND FUNCTION('DATE', m.uploadTime) = :date")
    Page<Media> findMediaByInstitutionAndDate(
            @Param("institutionId") Long institutionId,
            @Param("date") LocalDate date,
            Pageable pageable);
    
    /**
     * 根据日期查询所有机构的媒体列表
     * 
     * @param date 日期
     * @param pageable 分页参数
     * @return 媒体分页
     */
    @Query("SELECT m FROM Media m " +
           "WHERE FUNCTION('DATE', m.uploadTime) = :date")
    Page<Media> findAllMediaByDate(
            @Param("date") LocalDate date,
            Pageable pageable);
} 