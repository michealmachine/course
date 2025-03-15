'use client';

import { request } from './api';
import { ApiResponse, PaginationResult } from '@/types/api';
import { AxiosResponse } from 'axios';
import { Question, QuestionDTO, QuestionQueryParams } from '@/types/question';

/**
 * 问题管理服务
 */
const questionService = {
  /**
   * 获取问题列表（分页）
   */
  getQuestionList: async (params: QuestionQueryParams): Promise<PaginationResult<Question>> => {
    try {
      const { page = 0, pageSize = 10, institutionId, type, difficulty, title, search, tagIds, ...restParams } = params;

      // 将title和search参数都重命名为keyword
      let keyword = title || search;

      // 创建请求参数对象
      const requestParams: Record<string, any> = {
        page,
        pageSize,
        institutionId,
        ...restParams
      };

      // 仅当有值时添加可选参数
      if (type !== undefined) requestParams.type = type;
      if (difficulty !== undefined) requestParams.difficulty = difficulty;
      if (keyword) requestParams.keyword = keyword;

      // 处理tagIds数组 - 如果是数组且有元素，转换为逗号分隔的字符串
      if (tagIds && Array.isArray(tagIds) && tagIds.length > 0) {
        requestParams.tagIds = tagIds.join(',');
      }

      // 添加调试日志
      console.log('请求参数:', JSON.stringify(requestParams));

      const response: AxiosResponse<ApiResponse<PaginationResult<Question>>> = await request.get('/v1/questions', { params: requestParams });
      return response.data.data;
    } catch (error) {
      console.error('获取问题列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取单个问题详情
   */
  getQuestionById: async (id: number): Promise<Question> => {
    try {
      const response: AxiosResponse<ApiResponse<Question>> = await request.get(`/v1/questions/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取问题详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建问题
   */
  createQuestion: async (question: QuestionDTO): Promise<Question> => {
    try {
      const response: AxiosResponse<ApiResponse<Question>> = await request.post('/v1/questions', question);
      return response.data.data;
    } catch (error) {
      console.error('创建问题失败:', error);
      throw error;
    }
  },

  /**
   * 更新问题
   */
  updateQuestion: async (id: number, question: QuestionDTO): Promise<Question> => {
    try {
      const response: AxiosResponse<ApiResponse<Question>> = await request.put(`/v1/questions/${id}`, question);
      return response.data.data;
    } catch (error) {
      console.error(`更新问题失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除问题
   */
  deleteQuestion: async (questionId: number): Promise<void> => {
    try {
      await request.delete(`/v1/questions/${questionId}`);
    } catch (error) {
      console.error(`删除题目失败, ID: ${questionId}:`, error);
      throw error;
    }
  },

  /**
   * 批量删除问题
   */
  batchDeleteQuestions: async (ids: number[]): Promise<void> => {
    try {
      await request.delete('/v1/questions/batch', { data: ids });
    } catch (error) {
      console.error('批量删除问题失败:', error);
      throw error;
    }
  },

  /**
   * 检查题目是否被引用
   */
  checkQuestionReferences: async (questionId: number, institutionId: number): Promise<{ isReferenced: boolean; references: any }> => {
    try {
      const response: AxiosResponse<ApiResponse<{ isReferenced: boolean; references: any }>> = 
        await request.get(`/v1/questions/${questionId}/check-references`, { 
          params: { institutionId } 
        });
      return response.data.data;
    } catch (error) {
      console.error(`检查题目引用失败, ID: ${questionId}:`, error);
      throw error;
    }
  }
};

export default questionService; 