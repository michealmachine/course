'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { ReviewTask, ReviewResponseDTO, ReviewType, ReviewStatus } from '@/types/review';
import { CourseStructureVO } from '@/types/course';
import { AxiosResponse } from 'axios';

/**
 * 审核服务
 */
export const reviewService = {
  /**
   * 获取所有审核课程列表 (待审核课程)
   * @param page 页码或分页对象
   * @param size 每页数量
   */
  getAllCourses: async (page: number | any = 0, size: number = 10): Promise<any> => {
    try {
      // 确保page是数字
      const pageNum = typeof page === 'object' ? 0 : page;
      
      const response: AxiosResponse<ApiResponse<any>> = await request.get<any>(
        `/reviewer/courses/pending?page=${pageNum}&size=${size}`
      );
      console.log('审核课程列表响应:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取审核课程列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取正在审核的课程列表
   * @param page 页码
   * @param size 每页数量
   */
  getReviewingCourses: async (page: number = 0, size: number = 10): Promise<any> => {
    try {
      const response: AxiosResponse<ApiResponse<any>> = await request.get<any>(
        `/reviewer/courses/reviewing?page=${page}&size=${size}`
      );
      console.log('正在审核课程列表响应:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取正在审核课程列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取审核任务列表
   * @param page 页码
   * @param size 每页数量
   */
  getReviewTasks: async (page: number = 0, size: number = 10): Promise<any> => {
    try {
      const response: AxiosResponse<ApiResponse<any>> = await request.get<any>(
        `/reviews?page=${page}&size=${size}`
      );
      console.log('审核任务列表响应:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取审核任务列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取审核任务详情
   * @param reviewId 审核ID
   */
  getReviewTask: async (reviewId: string): Promise<ReviewTask> => {
    try {
      const response: AxiosResponse<ApiResponse<ReviewTask>> = await request.get<ReviewTask>(
        `/reviews/${reviewId}/task`
      );
      console.log('审核任务详情响应:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取审核任务失败:', error);
      throw error;
    }
  },

  /**
   * 获取课程结构
   * @param courseId 课程ID
   */
  getCourseStructure: async (courseId: number): Promise<CourseStructureVO> => {
    try {
      const response: AxiosResponse<ApiResponse<CourseStructureVO>> = await request.get<CourseStructureVO>(
        `/reviewer/courses/${courseId}/structure`
      );
      console.log('课程结构响应:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取课程结构失败:', error);
      throw error;
    }
  },

  /**
   * 获取小节媒体内容
   * @param sectionId 小节ID
   */
  getSectionMedia: async (sectionId: string | number): Promise<any> => {
    try {
      const response: AxiosResponse<ApiResponse<any>> = await request.get<any>(
        `/preview/resources/sections/${sectionId}/media`
      );
      return response.data.data;
    } catch (error) {
      console.error('获取小节媒体失败:', error);
      throw error;
    }
  },

  /**
   * 获取小节题组内容
   * @param sectionId 小节ID
   */
  getSectionQuestionGroup: async (sectionId: string | number): Promise<any> => {
    try {
      const response: AxiosResponse<ApiResponse<any>> = await request.get<any>(
        `/preview/resources/sections/${sectionId}/question-group`
      );
      return response.data.data;
    } catch (error) {
      console.error('获取小节题组失败:', error);
      throw error;
    }
  },

  /**
   * 开始审核课程
   * @param courseId 课程ID
   */
  startReview: async (courseId: number): Promise<any> => {
    try {
      const response: AxiosResponse<ApiResponse<any>> = await request.post<any>(
        `/reviewer/courses/${courseId}/review/start`
      );
      return response.data.data;
    } catch (error) {
      console.error('开始审核课程失败:', error);
      throw error;
    }
  },

  /**
   * 通过课程审核
   * @param courseId 课程ID
   * @param comment 审核意见
   */
  approveCourse: async (courseId: string, comment: string): Promise<void> => {
    try {
      const response: AxiosResponse<ApiResponse<void>> = await request.post<void>(
        `/reviewer/courses/${courseId}/review/approve?comment=${comment || ''}`
      );
      return response.data.data;
    } catch (error) {
      console.error('通过课程审核失败:', error);
      throw error;
    }
  },

  /**
   * 拒绝课程审核
   * @param courseId 课程ID
   * @param comment 拒绝原因
   */
  rejectCourse: async (courseId: string, comment: string): Promise<void> => {
    try {
      const data = { reason: comment };
      const response: AxiosResponse<ApiResponse<void>> = await request.post<void>(
        `/reviewer/courses/${courseId}/review/reject`,
        data
      );
      return response.data.data;
    } catch (error) {
      console.error('拒绝课程审核失败:', error);
      throw error;
    }
  },

  /**
   * 获取课程详情
   * @param courseId 课程ID
   */
  getCourseById: async (courseId: number): Promise<any> => {
    try {
      const response: AxiosResponse<ApiResponse<any>> = await request.get<any>(
        `/reviewer/courses/${courseId}`
      );
      return response.data.data;
    } catch (error) {
      console.error('获取课程详情失败:', error);
      throw error;
    }
  }
};

// 添加默认导出
export default reviewService; 