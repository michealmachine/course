package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.vo.LearningStatisticsVO;

/**
 * 学习统计服务接口
 */
public interface LearningStatisticsService {
    
    /**
     * 获取用户的学习统计数据
     *
     * @param userId 用户ID
     * @return 学习统计数据
     */
    LearningStatisticsVO getUserLearningStatistics(Long userId);
    
    /**
     * 获取用户特定课程的学习统计数据
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 学习统计数据
     */
    LearningStatisticsVO.CourseStatisticsVO getUserCourseLearningStatistics(Long userId, Long courseId);
    
    /**
     * 重置用户课程学习进度
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    void resetUserCourseProgress(Long userId, Long courseId);
} 