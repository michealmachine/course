package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.LearningRecordCompletedDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordEndDTO;
import com.zhangziqi.online_course_mine.model.dto.LearningRecordStartDTO;
import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.LearningRecordVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 学习记录服务接口
 */
public interface LearningRecordService {
    
    /**
     * 开始一个学习活动
     * @param userId 用户ID
     * @param dto 学习活动开始DTO
     * @return 创建的学习记录VO
     */
    LearningRecordVO startActivity(Long userId, LearningRecordStartDTO dto);
    
    /**
     * 结束一个学习活动
     * @param userId 用户ID
     * @param recordId 记录ID
     * @param dto 学习活动结束DTO
     * @return 更新后的学习记录VO
     */
    LearningRecordVO endActivity(Long userId, Long recordId, LearningRecordEndDTO dto);
    
    /**
     * 一次性记录已完成的学习活动
     * @param userId 用户ID
     * @param dto 已完成学习活动DTO
     * @return 创建的学习记录VO
     */
    LearningRecordVO recordCompletedActivity(Long userId, LearningRecordCompletedDTO dto);
    
    /**
     * 查找用户当前进行中的活动
     * @param userId 用户ID
     * @return 可选的学习记录VO
     */
    Optional<LearningRecordVO> findOngoingActivity(Long userId);
    
    /**
     * 获取用户学习记录（分页）
     * @param userId 用户ID
     * @param pageable 分页信息
     * @return 分页学习记录
     */
    Page<LearningRecordVO> getUserActivities(Long userId, Pageable pageable);
    
    /**
     * 获取用户特定课程的学习记录（分页）
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param pageable 分页信息
     * @return 分页学习记录
     */
    Page<LearningRecordVO> getUserCourseActivities(Long userId, Long courseId, Pageable pageable);
    
    /**
     * 获取用户每日学习统计
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日学习统计列表
     */
    List<DailyLearningStatVO> getDailyLearningStats(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户活动类型统计
     * @param userId 用户ID
     * @return 活动类型统计列表
     */
    List<ActivityTypeStatVO> getActivityTypeStats(Long userId);
    
    /**
     * 获取用户今日学习时长
     * @param userId 用户ID
     * @return 学习时长（秒）
     */
    Long getTodayLearningDuration(Long userId);
    
    /**
     * 获取用户总学习时长
     * @param userId 用户ID
     * @return 学习时长（秒）
     */
    Long getTotalLearningDuration(Long userId);
} 