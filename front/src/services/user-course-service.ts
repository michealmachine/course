'use client';

import { request } from './api';
import { ApiResponse, PaginationResult } from '@/types/api';
import { UserCourseVO, LearningProgressDTO, LearningDurationDTO } from '@/types/userCourse';
import { Course } from '@/types/course';
import { AxiosResponse } from 'axios';

/**
 * 用户课程服务 - 处理用户课程相关API
 */
const userCourseService = {
  /**
   * 获取用户已购课程列表
   */
  getUserPurchasedCourses: async (): Promise<UserCourseVO[]> => {
    try {
      const response = await request.get<UserCourseVO[]>('/user-courses');
      return response.data.data;
    } catch (error) {
      console.error('获取用户已购课程列表失败:', error);
      throw error;
    }
  },

  /**
   * 分页获取用户已购课程列表
   */
  getUserPurchasedCoursesWithPagination: async (page: number = 0, size: number = 10): Promise<PaginationResult<UserCourseVO>> => {
    try {
      const response = await request.get<PaginationResult<UserCourseVO>>('/user-courses/page', {
        params: { page, size }
      });
      return response.data.data;
    } catch (error) {
      console.error('分页获取用户已购课程列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户课程学习记录
   */
  getUserCourseRecord: async (courseId: number): Promise<UserCourseVO> => {
    try {
      const response = await request.get<UserCourseVO>(`/user-courses/${courseId}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取用户课程学习记录失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 更新用户课程学习进度
   */
  updateLearningProgress: async (courseId: number, chapterId: number, sectionId: number, sectionProgress: number): Promise<UserCourseVO> => {
    try {
      const response = await request.put<UserCourseVO>(
        `/user-courses/${courseId}/progress`,
        {
          chapterId,
          sectionId,
          sectionProgress
        }
      );
      return response.data.data;
    } catch (error) {
      console.error(`更新用户课程学习进度失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 记录用户学习时长
   */
  recordLearningDuration: async (courseId: number, duration: number): Promise<UserCourseVO> => {
    try {
      const response = await request.put<UserCourseVO>(
        `/user-courses/${courseId}/duration?duration=${duration}`
      );
      return response.data.data;
    } catch (error) {
      console.error(`记录用户学习时长失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 分页获取用户有效课程（状态正常的课程）
   */
  getUserValidCoursesWithPagination: async (page: number = 0, size: number = 10): Promise<PaginationResult<Course>> => {
    try {
      const response = await request.get<PaginationResult<Course>>('/user-courses/valid', {
        params: { page, size }
      });
      return response.data.data;
    } catch (error) {
      console.error('分页获取用户有效课程列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户最近学习的课程
   */
  getRecentLearnedCourses: async (limit: number = 5): Promise<UserCourseVO[]> => {
    try {
      const response = await request.get<UserCourseVO[]>(`/user-courses/recent?limit=${limit}`);
      return response.data.data;
    } catch (error) {
      console.error('获取用户最近学习的课程失败:', error);
      throw error;
    }
  }
};

export default userCourseService; 