package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程Repository
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    
    /**
     * 根据机构查找课程（分页）
     * 
     * @param institution 机构
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByInstitution(Institution institution, Pageable pageable);
    
    /**
     * 根据机构和创建者ID查找课程（分页）
     * 
     * @param institution 机构
     * @param creatorId 创建者ID
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByInstitutionAndCreatorId(Institution institution, Long creatorId, Pageable pageable);
    
    /**
     * 根据机构和课程状态查找课程（分页）
     * 
     * @param institution 机构
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByInstitutionAndStatus(Institution institution, Integer status, Pageable pageable);
    
    /**
     * 根据机构ID和课程ID查找课程
     * 
     * @param courseId 课程ID
     * @param institutionId 机构ID
     * @return 课程
     */
    @Query("SELECT c FROM Course c WHERE c.id = :courseId AND c.institution.id = :institutionId")
    Optional<Course> findByIdAndInstitutionId(@Param("courseId") Long courseId, @Param("institutionId") Long institutionId);
    
    /**
     * 根据发布版本ID查找工作副本
     * 
     * @param publishedVersionId 发布版本ID
     * @return 工作副本
     */
    Optional<Course> findByPublishedVersionId(Long publishedVersionId);
    
    /**
     * 查找工作区版本对应的发布版本
     * 
     * @param workspaceId 工作区版本ID
     * @return 发布版本
     */
    @Query("SELECT c FROM Course c WHERE c.publishedVersionId = :workspaceId AND c.isPublishedVersion = true")
    Optional<Course> findPublishedVersionByWorkspaceId(@Param("workspaceId") Long workspaceId);
    
    /**
     * 根据课程状态查找课程（分页）
     * 
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据分类ID查找课程（分页）
     * 
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 课程分页
     */
    @Query("SELECT c FROM Course c WHERE c.category.id = :categoryId")
    Page<Course> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * 根据分类ID统计课程数量
     * 
     * @param categoryId 分类ID
     * @return 课程数量
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 根据标题模糊查询课程（分页）
     * 
     * @param title 标题关键词
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByTitleContaining(String title, Pageable pageable);
    
    /**
     * 查找是否为发布版本的课程（分页）
     * 
     * @param isPublishedVersion 是否为发布版本
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByIsPublishedVersion(Boolean isPublishedVersion, Pageable pageable);
    
    /**
     * 根据机构ID和isPublishedVersion字段查询课程
     * 
     * @param institution 机构
     * @param isPublishedVersion 是否为发布版本
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByInstitutionAndIsPublishedVersion(Institution institution, Boolean isPublishedVersion, Pageable pageable);
    
    /**
     * 根据机构ID和发布版本状态查询课程
     * 
     * @param institutionId 机构ID
     * @param isPublishedVersion 发布版本状态
     * @param pageable 分页参数
     * @return 课程分页
     */
    @Query("SELECT c FROM Course c WHERE c.institution.id = :institutionId AND c.isPublishedVersion = :isPublishedVersion")
    Page<Course> findByInstitutionIdAndIsPublishedVersion(Long institutionId, Boolean isPublishedVersion, Pageable pageable);
    
    /**
     * 根据状态和是否为发布版本查询课程
     * 
     * @param status 课程状态
     * @param isPublishedVersion 是否为发布版本
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<Course> findByStatusAndIsPublishedVersion(Integer status, Boolean isPublishedVersion, Pageable pageable);
} 