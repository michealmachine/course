package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.vo.UserFavoriteVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户收藏课程服务接口
 */
public interface UserFavoriteService {
    
    /**
     * 添加收藏
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Long courseId);
    
    /**
     * 取消收藏
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long courseId);
    
    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Long courseId);
    
    /**
     * 获取用户收藏的课程列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 收藏课程分页列表
     */
    Page<UserFavoriteVO> getUserFavorites(Long userId, Pageable pageable);
    
    /**
     * 统计用户收藏课程数量
     * @param userId 用户ID
     * @return 收藏课程数量
     */
    long countUserFavorites(Long userId);
    
    /**
     * 统计课程被收藏次数
     * @param courseId 课程ID
     * @return 被收藏次数
     */
    long countCourseFavorites(Long courseId);
} 