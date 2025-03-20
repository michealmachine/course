package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.UserWrongQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户错题记录Repository
 */
@Repository
public interface UserWrongQuestionRepository extends JpaRepository<UserWrongQuestion, Long> {
    
    /**
     * 根据用户ID和课程ID和题目ID查找错题记录
     */
    Optional<UserWrongQuestion> findByUser_IdAndCourse_IdAndQuestionId(Long userId, Long courseId, Long questionId);
    
    /**
     * 根据用户ID和题目ID查找错题记录
     */
    Optional<UserWrongQuestion> findByUser_IdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 获取用户的所有错题
     */
    List<UserWrongQuestion> findByUser_Id(Long userId);
    
    /**
     * 获取用户的所有错题（分页）
     */
    Page<UserWrongQuestion> findByUser_Id(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和状态查找错题
     */
    List<UserWrongQuestion> findByUser_IdAndStatus(Long userId, Integer status);
    
    /**
     * 根据用户ID和状态查找错题（分页）
     */
    Page<UserWrongQuestion> findByUser_IdAndStatus(Long userId, Integer status, Pageable pageable);
    
    /**
     * 获取用户指定课程的所有错题
     */
    List<UserWrongQuestion> findByUser_IdAndCourse_Id(Long userId, Long courseId);
    
    /**
     * 获取用户指定课程的所有错题（分页）
     */
    Page<UserWrongQuestion> findByUser_IdAndCourse_Id(Long userId, Long courseId, Pageable pageable);
    
    /**
     * 获取用户指定课程和状态的错题
     */
    List<UserWrongQuestion> findByUser_IdAndCourse_IdAndStatus(Long userId, Long courseId, Integer status);
    
    /**
     * 获取用户指定课程和状态的错题（分页）
     */
    Page<UserWrongQuestion> findByUser_IdAndCourse_IdAndStatus(Long userId, Long courseId, Integer status, Pageable pageable);
    
    /**
     * 获取用户错题总数
     */
    long countByUser_Id(Long userId);
    
    /**
     * 获取用户指定课程的错题总数
     */
    long countByUser_IdAndCourse_Id(Long userId, Long courseId);
    
    /**
     * 获取用户指定状态的错题总数
     */
    long countByUser_IdAndStatus(Long userId, Integer status);
    
    /**
     * 获取用户指定课程和状态的错题总数
     */
    long countByUser_IdAndCourse_IdAndStatus(Long userId, Long courseId, Integer status);
    
    /**
     * 删除用户的所有错题
     */
    void deleteByUser_Id(Long userId);
    
    /**
     * 删除用户指定课程的所有错题
     */
    void deleteByUser_IdAndCourse_Id(Long userId, Long courseId);
} 