package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 小节Repository
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    
    /**
     * 根据章节查找小节，按排序索引排序
     * 
     * @param chapter 章节
     * @return 小节列表
     */
    List<Section> findByChapterOrderByOrderIndexAsc(Chapter chapter);
    
    /**
     * 根据章节ID查找小节，按排序索引排序
     * 
     * @param chapterId 章节ID
     * @return 小节列表
     */
    List<Section> findByChapter_IdOrderByOrderIndexAsc(Long chapterId);
    
    /**
     * 根据章节ID删除所有小节
     * 
     * @param chapterId 章节ID
     */
    void deleteByChapter_Id(Long chapterId);
    
    /**
     * 根据内容类型查找小节
     * 
     * @param contentType 内容类型
     * @return 小节列表
     */
    List<Section> findByContentType(String contentType);
    
    /**
     * 查找章节下最大的排序索引
     * 
     * @param chapterId 章节ID
     * @return 最大排序索引
     */
    Integer findMaxOrderIndexByChapter_Id(Long chapterId);
    
    /**
     * 根据课程ID查找所有小节
     * 
     * @param courseId 课程ID
     * @return 小节列表
     */
    @Query("SELECT s FROM Section s JOIN s.chapter c WHERE c.course.id = :courseId ORDER BY c.orderIndex, s.orderIndex")
    List<Section> findByCourseIdOrderByChapterOrderIndexAndOrderIndexAsc(@Param("courseId") Long courseId);
} 