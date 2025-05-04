'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse } from '@/types/api';
import { UserWrongQuestionVO, WrongQuestionPageResult, WrongQuestionQueryParams } from '@/types/wrongQuestion';

/**
 * 错题服务 - 处理错题相关API
 */
const wrongQuestionService = {
  /**
   * 获取用户错题列表(分页)
   */
  getWrongQuestions: async (params: WrongQuestionQueryParams = {}): Promise<WrongQuestionPageResult> => {
    try {
      const { page = 0, size = 10, sortBy = 'updatedAt', direction = 'desc' } = params;

      const response: AxiosResponse<ApiResponse<WrongQuestionPageResult>> =
        await request.get('/learning/wrong-questions', {
          params: { page, size, sortBy, direction }
        });

      return response.data.data;
    } catch (error) {
      console.error('获取用户错题列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取特定课程的错题列表(分页)
   */
  getCourseWrongQuestions: async (courseId: number, params: WrongQuestionQueryParams = {}): Promise<WrongQuestionPageResult> => {
    try {
      const { page = 0, size = 10, sortBy = 'updatedAt', direction = 'desc' } = params;

      const response: AxiosResponse<ApiResponse<WrongQuestionPageResult>> =
        await request.get(`/learning/courses/${courseId}/wrong-questions`, {
          params: { page, size, sortBy, direction }
        });

      return response.data.data;
    } catch (error) {
      console.error(`获取课程错题列表失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取未解决的错题列表(分页)
   */
  getUnresolvedWrongQuestions: async (params: WrongQuestionQueryParams = {}): Promise<WrongQuestionPageResult> => {
    try {
      const { page = 0, size = 10, sortBy = 'updatedAt', direction = 'desc' } = params;

      const response: AxiosResponse<ApiResponse<WrongQuestionPageResult>> =
        await request.get('/learning/wrong-questions/unresolved', {
          params: { page, size, sortBy, direction }
        });

      return response.data.data;
    } catch (error) {
      console.error('获取未解决错题列表失败:', error);
      throw error;
    }
  },

  /**
   * 将错题标记为已解决
   */
  resolveWrongQuestion: async (wrongQuestionId: number): Promise<void> => {
    try {
      await request.put(`/learning/wrong-questions/${wrongQuestionId}/resolve`);
    } catch (error) {
      console.error(`标记错题为已解决失败, 错题ID: ${wrongQuestionId}:`, error);
      throw error;
    }
  },

  /**
   * 删除单个错题
   */
  deleteWrongQuestion: async (wrongQuestionId: number): Promise<void> => {
    try {
      await request.delete(`/learning/wrong-questions/${wrongQuestionId}`);
    } catch (error) {
      console.error(`删除错题失败, 错题ID: ${wrongQuestionId}:`, error);
      throw error;
    }
  },

  /**
   * 删除用户所有错题
   */
  deleteAllWrongQuestions: async (): Promise<void> => {
    try {
      await request.delete('/learning/wrong-questions/all');
    } catch (error) {
      console.error('删除所有错题失败:', error);
      throw error;
    }
  },

  /**
   * 删除用户特定课程的所有错题
   */
  deleteAllCourseWrongQuestions: async (courseId: number): Promise<void> => {
    try {
      await request.delete(`/learning/courses/${courseId}/wrong-questions/all`);
    } catch (error) {
      console.error(`删除课程所有错题失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }
};

export default wrongQuestionService;