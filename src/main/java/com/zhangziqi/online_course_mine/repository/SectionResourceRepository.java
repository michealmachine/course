package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.SectionResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 小节资源Repository
 */
@Repository
public interface SectionResourceRepository extends JpaRepository<SectionResource, Long> {
    
    /**
     * 根据小节ID查找资源
     * 
     * @param sectionId 小节ID
     * @return 小节资源列表
     */
    List<SectionResource> findBySection_Id(Long sectionId);
    
    /**
     * 根据小节查找资源，按排序索引排序
     * 
     * @param section 小节
     * @return 小节资源列表
     */
    List<SectionResource> findBySectionOrderByOrderIndexAsc(Section section);
    
    /**
     * 根据小节ID删除所有资源
     * 
     * @param sectionId 小节ID
     */
    void deleteBySection_Id(Long sectionId);
    
    /**
     * 根据媒体ID查找使用该媒体的小节资源
     * 
     * @param mediaId 媒体ID
     * @return 小节资源列表
     */
    List<SectionResource> findByMedia_Id(Long mediaId);
    
    /**
     * 根据资源类型查找小节资源
     * 
     * @param resourceType 资源类型
     * @return 小节资源列表
     */
    List<SectionResource> findByResourceType(String resourceType);
    
    /**
     * 根据课程ID查找所有小节资源
     * 
     * @param courseId 课程ID
     * @return 小节资源列表
     */
    @Query("SELECT sr FROM SectionResource sr JOIN sr.section s JOIN s.chapter c WHERE c.course.id = :courseId")
    List<SectionResource> findByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 检查媒体是否被任何小节资源引用
     * 
     * @param mediaId 媒体ID
     * @return 引用数量
     */
    long countByMedia_Id(Long mediaId);
} 