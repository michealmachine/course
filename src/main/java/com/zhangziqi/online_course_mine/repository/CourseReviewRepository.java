package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 课程评论数据访问接口
 */
@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long>, JpaSpecificationExecutor<CourseReview> {
    
    /**
     * 根据课程ID分页查找评论
     */
    @Query("SELECT r FROM CourseReview r WHERE r.course.id = :courseId")
    Page<CourseReview> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);
    
    /**
     * 根据课程实体分页查找评论
     */
    Page<CourseReview> findByCourse(Course course, Pageable pageable);
    
    /**
     * 根据用户ID和课程ID查找评论
     */
    @Query("SELECT r FROM CourseReview r WHERE r.userId = :userId AND r.course.id = :courseId")
    Optional<CourseReview> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    /**
     * 计算课程特定评分的评论数量
     */
    @Query("SELECT COUNT(r) FROM CourseReview r WHERE r.course.id = :courseId AND r.rating = :rating")
    long countByCourseIdAndRating(@Param("courseId") Long courseId, @Param("rating") Integer rating);
    
    /**
     * 删除指定用户对指定课程的评论
     */
    @Query("DELETE FROM CourseReview r WHERE r.userId = :userId AND r.course.id = :courseId")
    void deleteByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    /**
     * 查找用户是否评论过特定课程
     */
    @Query("SELECT COUNT(r) > 0 FROM CourseReview r WHERE r.userId = :userId AND r.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    /**
     * 计算课程评论总数
     */
    @Query("SELECT COUNT(r) FROM CourseReview r WHERE r.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 计算用户评论总数
     */
    long countByUserId(Long userId);
} 