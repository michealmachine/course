package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 章节Repository
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    /**
     * 根据课程查找章节，按排序索引排序
     * 
     * @param course 课程
     * @return 章节列表
     */
    List<Chapter> findByCourseOrderByOrderIndexAsc(Course course);
    
    /**
     * 根据课程ID查找章节，按排序索引排序
     * 
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<Chapter> findByCourse_IdOrderByOrderIndexAsc(Long courseId);
    
    /**
     * 根据课程ID删除所有章节
     * 
     * @param courseId 课程ID
     */
    void deleteByCourse_Id(Long courseId);
    
    /**
     * 查找课程下最大的排序索引
     * 
     * @param courseId 课程ID
     * @return 最大排序索引
     */
    Integer findMaxOrderIndexByCourse_Id(Long courseId);
} 