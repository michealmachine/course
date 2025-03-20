package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    
    /**
     * 查询用户是否已收藏某课程
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 收藏记录
     */
    Optional<UserFavorite> findByUser_IdAndCourse_Id(Long userId, Long courseId);
    
    /**
     * 查询用户收藏的所有课程
     * @param userId 用户ID
     * @return 收藏记录列表
     */
    List<UserFavorite> findByUser_Id(Long userId);
    
    /**
     * 分页查询用户收藏的课程
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<UserFavorite> findByUser_Id(Long userId, Pageable pageable);
    
    /**
     * 删除指定用户对指定课程的收藏
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 删除数量
     */
    @Modifying
    @Query("DELETE FROM UserFavorite uf WHERE uf.user.id = :userId AND uf.course.id = :courseId")
    int deleteByUserAndCourseIds(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    /**
     * 统计课程被收藏次数
     * @param courseId 课程ID
     * @return 收藏次数
     */
    long countByCourse_Id(Long courseId);
} 