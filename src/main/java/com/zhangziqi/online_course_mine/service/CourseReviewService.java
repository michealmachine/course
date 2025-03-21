package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.review.ReviewCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.review.ReviewQueryDTO;
import com.zhangziqi.online_course_mine.model.vo.CourseReviewSectionVO;
import com.zhangziqi.online_course_mine.model.vo.ReviewStatsVO;
import com.zhangziqi.online_course_mine.model.vo.ReviewVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 课程评论服务接口
 */
public interface CourseReviewService {

    /**
     * 创建课程评论
     *
     * @param dto 评论创建DTO
     * @param userId 用户ID
     * @return 创建的评论
     */
    ReviewVO createReview(ReviewCreateDTO dto, Long userId);
    
    /**
     * 获取课程评论区（包含统计和评论列表）
     *
     * @param courseId 课程ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param orderBy 排序方式
     * @return 评论区数据
     */
    CourseReviewSectionVO getCourseReviewSection(Long courseId, Integer page, Integer size, String orderBy);
    
    /**
     * 分页获取课程评论
     *
     * @param courseId 课程ID
     * @param queryDTO 查询参数
     * @param pageable 分页参数
     * @return 评论分页结果
     */
    Page<ReviewVO> getReviewsByCourse(Long courseId, ReviewQueryDTO queryDTO, Pageable pageable);
    
    /**
     * 获取评论统计数据
     *
     * @param courseId 课程ID
     * @return 评论统计
     */
    ReviewStatsVO getReviewStats(Long courseId);
    
    /**
     * 删除评论
     *
     * @param reviewId 评论ID
     * @param userId 用户ID
     */
    void deleteReview(Long reviewId, Long userId);
    
    /**
     * 获取用户对课程的评论
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 用户的评论
     */
    ReviewVO getUserReviewOnCourse(Long userId, Long courseId);
    
    /**
     * 更新课程评论
     *
     * @param reviewId 评论ID
     * @param dto 评论更新DTO
     * @param userId 用户ID
     * @return 更新后的评论
     */
    ReviewVO updateReview(Long reviewId, ReviewCreateDTO dto, Long userId);
} 