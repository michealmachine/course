'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { 
  ReviewVO, 
  CourseReviewSectionVO, 
  ReviewStatsVO, 
  ReviewCreateDTO,
  ReviewSortOrder,
  ReviewPage
} from '@/types/course-review';
import { AxiosResponse, AxiosError } from 'axios';

/**
 * 课程评论服务
 */
const courseReviewService = {
  /**
   * 获取课程评论区（包含统计和评论列表）
   * @param courseId 课程ID
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @param orderBy 排序方式
   */
  getCourseReviewSection: async (
    courseId: number, 
    page: number = 0, 
    size: number = 10, 
    orderBy: ReviewSortOrder = ReviewSortOrder.NEWEST
  ): Promise<CourseReviewSectionVO> => {
    try {
      const response: AxiosResponse<ApiResponse<CourseReviewSectionVO>> = 
        await request.get(`/courses/${courseId}/reviews`, {
          params: { page, size, orderBy }
        });
      
      return response.data.data;
    } catch (error) {
      console.error('获取课程评论区失败:', error);
      throw error;
    }
  },

  /**
   * 获取评论统计数据
   * @param courseId 课程ID
   */
  getReviewStats: async (courseId: number): Promise<ReviewStatsVO> => {
    try {
      const response: AxiosResponse<ApiResponse<ReviewStatsVO>> = 
        await request.get(`/courses/${courseId}/reviews/stats`);
      
      return response.data.data;
    } catch (error) {
      console.error('获取评论统计数据失败:', error);
      throw error;
    }
  },

  /**
   * 获取当前用户对课程的评论
   * @param courseId 课程ID
   */
  getUserReviewOnCourse: async (courseId: number): Promise<ReviewVO | null> => {
    try {
      const response: AxiosResponse<ApiResponse<ReviewVO>> = 
        await request.get(`/courses/${courseId}/reviews/mine`);
      
      return response.data.data;
    } catch (error: any) {
      // 如果用户没有评论过，API会返回404，这种情况我们返回null而不是抛出错误
      if (error.response && error.response.status === 404) {
        return null;
      }
      console.error('获取用户评论失败:', error);
      throw error;
    }
  },

  /**
   * 创建评论
   * @param reviewData 评论数据
   */
  createReview: async (reviewData: ReviewCreateDTO): Promise<ReviewVO> => {
    try {
      const response: AxiosResponse<ApiResponse<ReviewVO>> = 
        await request.post('/courses/reviews', reviewData);
      
      return response.data.data;
    } catch (error) {
      console.error('创建评论失败:', error);
      throw error;
    }
  },

  /**
   * 更新评论
   * @param reviewId 评论ID
   * @param reviewData 评论数据
   */
  updateReview: async (reviewId: number, reviewData: ReviewCreateDTO): Promise<ReviewVO> => {
    try {
      const response: AxiosResponse<ApiResponse<ReviewVO>> = 
        await request.put(`/courses/reviews/${reviewId}`, reviewData);
      
      return response.data.data;
    } catch (error) {
      console.error('更新评论失败:', error);
      throw error;
    }
  },

  /**
   * 删除评论
   * @param reviewId 评论ID
   */
  deleteReview: async (reviewId: number): Promise<void> => {
    try {
      await request.delete(`/courses/reviews/${reviewId}`);
    } catch (error) {
      console.error('删除评论失败:', error);
      throw error;
    }
  },

  /**
   * 分页获取课程评论（支持筛选和排序）
   * @param courseId 课程ID
   * @param page 页码
   * @param size 每页大小
   * @param ratingFilter 评分筛选
   * @param orderBy 排序方式
   */
  getReviewsByCourse: async (
    courseId: number,
    page: number = 0,
    size: number = 10,
    ratingFilter?: number,
    orderBy: ReviewSortOrder = ReviewSortOrder.NEWEST
  ): Promise<ReviewPage> => {
    try {
      const params: any = { 
        page, 
        size,
        orderBy
      };
      
      if (ratingFilter) {
        params.ratingFilter = ratingFilter;
      }
      
      const response: AxiosResponse<ApiResponse<ReviewPage>> = 
        await request.get(`/courses/${courseId}/reviews/filter`, { params });
      
      return response.data.data;
    } catch (error) {
      console.error('获取课程评论失败:', error);
      throw error;
    }
  }
};

export default courseReviewService; 